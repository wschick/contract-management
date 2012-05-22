package controllers

// File uploader is http://valums.com/ajax-upload

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import anorm._

import java.io.File
import java.io.IOException
import org.joda.time.LocalDate

import models.Budget
import models.Company
import models.Contract
import models.ContractCosts
import models.ContractType
import models.Currency
import models.CSVLine
import models.DateOrMTM
import models.Location
import models.Person
import models.Term
import models.TimePeriodUnits
import FileHelper._
import scala.io.Source

object Imports extends Controller {

	var errors = List[String]()

	var lastFileName = ""

	var linesFromFile = List[CSVLine]()

	def resetState = {
		errors = List[String]()
		linesFromFile = List[CSVLine]()
		lastFileName = ""
	}

	def start = Action {
		resetState
    Ok(views.html.import_files.upload())
	}

	def resultsOfRead = Action {
    Ok(views.html.import_files.read_result(errors.reverse, lastFileName, linesFromFile))
	}

	/** Information about an error.
	
		@param fatal True if the error is severe enough that you should not create a contract.

	*/
	case class ErrorInfo(fileName: String, lineNum: Int, message: String, fatal: Boolean = false)

	val DEFAULT_CONTRACT_TYPE = "Line"
	
	val DEFAULT_BUDGET_ID = 1

	val DEFAULT_BUDGET: Budget = {
		val b = Budget.findById(DEFAULT_BUDGET_ID)
		if (b == None) throw new IllegalArgumentException("Budget with ID " + DEFAULT_BUDGET_ID + " doesn't exist.")
		b.get
	}

	val NA_LOCATION_ID = 1 // Default location id is NA, if you can't find it.

	val NA_LOCATION: Location = {
		val l = Location.findById(NA_LOCATION_ID)
		if (l == None) throw new IllegalArgumentException("NA Location (ID " + NA_LOCATION_ID + ") doesn't exist.")
		l.get
	}

	val DEFAULT_CURRENCY_ID = 1 

	val DEFAULT_CURRENCY: Currency = {
		val c = Currency.findById(DEFAULT_CURRENCY_ID)
		if (c == None) throw new IllegalArgumentException("Currency with ID " + DEFAULT_CURRENCY_ID + " doesn't exist.")
		c.get
	}

	val MISSING_DATE = new LocalDate(1, 1, 1) // Used in place of missing date

	val MISSING_TERM = new Term(1, TimePeriodUnits.DAY)

	def findOrCreateCompany(vendorName: String): (Company, Option[String]) = {
		val tempVendor = Company.findByName(vendorName)
		if (tempVendor != None) return (tempVendor.get, None)
		else {
			// Create a new vendor.
			val msg = "Created vendor \"" + vendorName + "\""
			val id = Company.create(vendorName, None)
			Logger.info("Created company " + vendorName + " with id " + id)
			return (Company(id, vendorName, None), Some(msg))
		}
	}

	// Get this string from parameters.
	val OUR_COMPANY_NAME_PARAM = "import.ourCompanyName"
	val OUR_COMPANY_NAME = Play.configuration.getString(OUR_COMPANY_NAME_PARAM)
	if (OUR_COMPANY_NAME == None) throw new IllegalArgumentException("No parameter " + OUR_COMPANY_NAME_PARAM)
	val (ourCompany, ourErrorMsg) = findOrCreateCompany(OUR_COMPANY_NAME.get)
	if (ourErrorMsg != None) throw new IllegalArgumentException("Error looking up our company: " + ourErrorMsg)

	def findOrCreatePerson(name: String, email: String, companyId: Long): (Person, Option[String]) = {
		val tempPerson = Person.findByName(name)
		if (tempPerson != None) return (tempPerson.get, None)
		else {
			// Create a new Person.
			val msg = "Created person \"" + name + "\" with email " + email
			val id = Person.create(name, email, None, companyId)
			Logger.info("Created person " + name + " with id " + id)
			return (Person(id, name, email, None, companyId), Some(msg))
		}
	}



	def importContract(info: CSVLine): (Option[Contract], Option[List[ErrorInfo]]) = {
		val errors = scala.collection.mutable.ListBuffer.empty[ErrorInfo]
		val shortErrors = scala.collection.mutable.ListBuffer.empty[ErrorInfo] // Very short error message.
		var fatalError = false;
		val fileName = "-"
		val lineNum = 0

		// Find the company
		val (vendor, vendorError) = findOrCreateCompany(info.vendor)
		vendorError.map(ve => {
			shortErrors += ErrorInfo(fileName, lineNum, "Vendor")
			errors += ErrorInfo(fileName, lineNum, ve)
		})

		// Make related people
		val (ourContact, ourContactError) = findOrCreatePerson(info.ourContact, "dummyemail", ourCompany.id)
		ourContactError.map(errmsg => {
			shortErrors += ErrorInfo(fileName, lineNum, "Our Contact")
			errors += ErrorInfo(fileName, lineNum, errmsg)
		})

		val (vendorContact, vendorContactError) = findOrCreatePerson(info.vendorContact, info.vendorEmail, vendor.id)
		vendorContactError.map(errmsg => {
			shortErrors += ErrorInfo(fileName, lineNum, "Vendor Contact")
			errors += ErrorInfo(fileName, lineNum, errmsg)
		})

		val vendorContractId = "???"
		val billingAccount = Some("???")
		val isMSA: Boolean = false
		val MSAId: Option[Long] = None
		val extraInfo = {
			if (info.lines == "") None
			else Some(info.lines)
		}
		val description: Option[String] = None

		val contractType = ContractType.findByName(DEFAULT_CONTRACT_TYPE)
		if (contractType == None) {
			val msg = "Can't find contract type \"" + DEFAULT_CONTRACT_TYPE + "\""
			errors += ErrorInfo(fileName, lineNum, msg, true)
			Logger.error(msg)
			fatalError = true
		}

		val aEnd: Location = {
			val tempLoc = Location.findByCode(info.aSite)
			if (tempLoc != None) tempLoc.get
			else {
				val msg = "Can't find A side code \"" + info.aSite + "\""
				shortErrors += ErrorInfo(fileName, lineNum, "A Side")
				errors += ErrorInfo(fileName, lineNum, msg)
				Logger.warn(msg)
				NA_LOCATION
			}
		}

		val zEnd: Location = {
			if (info.zSite == None) NA_LOCATION 
			else {
				val tempLoc = Location.findByCode(info.zSite.get)
				if (tempLoc != None) tempLoc.get
				else {
					val msg = "Can't find Z side code \"" + tempLoc + "\""
					shortErrors += ErrorInfo(fileName, lineNum, "Z Side")
					errors += ErrorInfo(fileName, lineNum, msg)
					Logger.warn(msg)
					NA_LOCATION
				}
			}
		}

		val cost: ContractCosts = {
			val currency = Currency.findByAbbreviation(info.currency)
			if (currency == None) {
				val msg = "Can't find currency \"" + info.currency + "\""
				shortErrors += ErrorInfo(fileName, lineNum, "Currency")
				errors += ErrorInfo(fileName, lineNum, msg)
				Logger.warn(msg)
				ContractCosts(info.mrc, info.nrc, DEFAULT_CURRENCY, DEFAULT_BUDGET)
			}
			else ContractCosts(info.mrc, info.nrc, currency.get, DEFAULT_BUDGET)
		}

		val startDate = {
			if (info.startDate != None) info.startDate.date.get
			else {
				val msg = "No starting date. I have: " + info.startDate
				shortErrors += ErrorInfo(fileName, lineNum, "start")
				errors += ErrorInfo(fileName, lineNum, msg)
				Logger.warn(msg)
				MISSING_DATE
			}
		}

		val contractTerm = {
			if (info.contractTerm.term != None) info.contractTerm.term.get
			else {
				val msg = "No term. I have: " + info.contractTerm
				shortErrors += ErrorInfo(fileName, lineNum, "term")
				errors += ErrorInfo(fileName, lineNum, msg)
				Logger.warn(msg)
				MISSING_TERM
			}
		}

		val earliestCancellationNotice = {
			if (info.earliestCancellationNotice.term != None) info.earliestCancellationNotice.term.get
			else {
				val msg = "No cancellation period. I have: " + info.earliestCancellationNotice
				shortErrors += ErrorInfo(fileName, lineNum, "cancel period")
				errors += ErrorInfo(fileName, lineNum, msg)
				Logger.warn(msg)
				MISSING_TERM
			}
		}


		val cancelledDate: Option[LocalDate] = None
		val autoRenewPeriod: Option[Term] = None
		val attentionText = shortErrors.foldLeft[String](""){(str, err) => err.message + "," + str}
		Logger.info(attentionText)

		if (!fatalError) {
			Logger.info("Creating contract")
			return (Some(Contract(
				NotAssigned,
				vendor,
				vendorContractId,
				billingAccount,
				isMSA,
				MSAId,
				extraInfo,
				description,
				contractType.get,
				aEnd,
				zEnd,
				cost,
				startDate,
				contractTerm,
				earliestCancellationNotice,
				cancelledDate,
				autoRenewPeriod,
				Some(attentionText)
				)), None)
		} else {
			Logger.info("Fatal error for contract")
			return (None, Some(errors.toList))
		}
	}

	/** Turn List of ErrorInfo into list of strings */
	def makeErrorStrings(errors: List[ErrorInfo]): List[String] = {
		val errStr = scala.collection.mutable.ListBuffer.empty[String]
		errors.foreach { error =>
			errStr += error.fileName + ":" + error.lineNum + " " + error.message
		}
		errStr.toList
	}

	def upload(numHeadingLines: Int) = Action(parse.temporaryFile) { implicit request =>
		println("Upload called")
		resetState
		lastFileName = request.queryString("qqfile").head
		Logger.info("________________" + linesFromFile.length + " " + lastFileName)

		// Read lines in the file. Make contracts from the lines.
		val src = Source.fromFile(request.body.file)

		errors = List[String]()
		var lineCounter = numHeadingLines + 1

		/** See if this date object has an error. Return true if it is ok. */
		def checkDateErrors(name: String, d: DateOrMTM): Boolean = {
			if (d.error != None) { 
				errors ::= "Line " + lineCounter + ": " + name + ": " + d.error.get; 
				return false 
			}
			else return true
		}


		src.getLines.drop(numHeadingLines).foreach(line => {
			try {
				Logger.debug(line)
				val l = CSVLine.parseLine(line, lastFileName, lineCounter)
				Logger.info("Got a line from vendor " + l.vendor)
				// Check for errors on dates
				if (checkDateErrors("Start date", l.startDate) &&
					checkDateErrors("End date", l.endDate) &&
					checkDateErrors("Renewal date", l.renewalDate) &&
					checkDateErrors("Earliest cancellation date", l.earliestCancellationDate)
					) linesFromFile ::= l
			} catch {
				case e: Exception => {
					Logger.error(e.getMessage)
					errors ::= "Line " + lineCounter + ": " + e.getMessage
					Logger.debug("Now " + errors.length + " errors")
				}
			}
			lineCounter += 1
		})

		if (errors.length == 0) {
			Ok("{\"success\": true}")
		} else {
			Logger.debug(errors.length + " errors")
			val result = "{\"success\": true,\n\"errors\": [\"" + 
			errors.reduceLeft[String]{(str, item) => str + "\", \"" + item } + "\"]}"
			Logger.debug(result)
			Ok(result)
		}

	}


	def save = Action {
		var importedLines = 0
		val errors = scala.collection.mutable.ListBuffer.empty[ErrorInfo]
		// Go through lines and import them.
		Logger.debug("Saving " + linesFromFile.length + " lines")
		linesFromFile.foreach { line => 
			{
				val (contract, errorsForContract) = importContract(line) 
				errorsForContract.map(e => errors ++= e)
				contract.map(c => Contract.create(c))
				importedLines += 1
			}
		}
		val tempLastFileName = lastFileName
		resetState

    Ok(views.html.import_files.save_result(makeErrorStrings(errors.toList), tempLastFileName, importedLines))
	}

}
