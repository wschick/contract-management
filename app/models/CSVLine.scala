package models

import java.text._
import org.joda.time._
import org.joda.time.format._
import play.api.Logger
import scala.util.matching.Regex
import scala.collection.mutable.ListBuffer

case class CSVLine(
	country: String,
	vendor: String,
	lines: String,
	quantity: Int,
	aSite: String,
	zSite: Option[String],
	startDate: DateOrMTM,
	endDate: DateOrMTM,
	renewalDate: DateOrMTM,
	earliestCancellationDate: DateOrMTM,
	contractTerm: MaybeTerm,
	earliestCancellationNotice: MaybeTerm,
	fineForEarlyCancel: String,
	monthlyCostInFX: String,
	nrc: Double,
	mrc: Double,
	currency: String,
	ourContact: String,
	vendorContact: String,
	vendorEmail: String,
	digitalContract: Boolean,
	physicalOriginal: Boolean
	)

/**
	Represents what we might see for a date string. It could be a date, it could be "M-T-M"
	or it could be something else.
	*/
case class DateOrMTM(
	date: Option[LocalDate],
	isMTM: Boolean,
	error: Option[String]
	)

object DateOrMTM {

	def parseDate(dateString: String): DateOrMTM = {

		if (dateString == "M-T-M") return DateOrMTM(None, true, None)
		try {
			val fmt: DateTimeFormatter = DateTimeFormat.forPattern("MM/dd/yyyy")
			val d = fmt.parseLocalDate(dateString)
			return DateOrMTM(Some(d), false, None)
		}
		catch {
			case e: Exception =>  {
				Logger.warn("Bad date \"" + dateString + "\": " + e.getMessage)
				return DateOrMTM(None, false, Some(dateString))
			}
		}
	}

}


/**
	Represents a possible term. We might find a string that isn't a term
*/
case class MaybeTerm(
	term: Option[Term],
	other: Option[String]
	)

object MaybeTerm {

	val daysRegex = """^(\d+)(()|( [Dd]ays)|( [Mm]onths?)|( [Yy]ears?))$""".r

	def parseTerm(termString: String): MaybeTerm = {
		// If M-T-M, just return a 1 month term
		if (termString == "M-T-M") return MaybeTerm(Some(Term(1, TimePeriodUnits.MONTH)), None)
		Logger.debug("Parsing term \"" + termString + "\"")
		try {
			val daysRegex(len, _, _, days, months, years ) = termString
			Logger.debug("Matching \"" + termString + "\"\nlen: " + len + " days " + days + " months " + months + " years " + years)
			if (months != null) return MaybeTerm(Some(Term(len.toInt, TimePeriodUnits.MONTH)), None)
			if (years != null) return MaybeTerm(Some(Term(len.toInt, TimePeriodUnits.YEAR)), None)
			else return MaybeTerm(Some(Term(len.toInt, TimePeriodUnits.DAY)), None)
		}
		catch {
			case e: MatchError => {
				Logger.error("Bad term \"" + termString + "\"")
				return MaybeTerm(None, Some(termString))
			}
		}
	}
}
		

object CSVLine {


	val enclosingQuotesPattern = """^"?\s*(.*?)\s*"?$""".r
	/** Trim off any enclosing double quotes and white space
	*/
	def trimString(str: String, index: Int = -1): String = {
		try {
			val enclosingQuotesPattern(result) = str
			Logger.debug("Input" + { if (index >= 0) { " " + index } else { ""} } + ": \"" + str + "\", output \"" + result + "\"")
			return result
		}
		catch {
			case e: MatchError => return str
		}
	}

	/**
		Parse a site string from a spreadsheet. The string may be of the following forms

			asite - zsite
			asite-zsite
			asite/zsite
			asite

			@param sites A string with 1 or 2 sites
			@returns A tuple with the sites splite. The second element will be None if there is only 1 site.
			If something is wrong - no sites, or more than 2 sites - this logs an error and returns the sites
			
			string as the first element of the tuple and None for the second.

	*/
	def parseSites(sites: String): (String, Option[String]) = {

		val strs = " *[/-] *".r.split(trimString(sites))

		strs.length match {
			case 1 => return (strs(0), None)
			case 2 => return (strs(0), Some(strs(1)))
			case _ => {
				Logger.error("Bad sites string \"" + sites + "\"")
				return (sites, None)
			}
		}
	}


	val costPattern = """^ *"? *(\$?) *(([a-zA-Z]{3})?) *((-|n/a)|([0-9,\.]+)) *(([a-zA-Z]{3})?) *"? *$""".r

	/**
	  Parse cost strings of the following form

		$48,000
		$48,000.34
		$ - 
		$4,000.00 AUD
		n/a
		420
		EUR 100.00
		DKK 430.00
		GBP 293.33
		AUD 2309
	
		@returns A tuple with the value and the currency. If you put in 2 currency names (e.g. EUR and USD), 
		the currency string is set to "???" Return 0 for no cost. 
		@throws MatchError if it can't match the usual pattern.
	*/
	def parseCost(str: String, fileName: String, lineNum: Int): (Double, Option[String])  = {

		Logger.debug("Parsing \"" + str + "\"")
		val costPattern(_, _, frontCurrency, _, noCost, amount, _, rearCurrency) = trimString(str)

		val currency = (frontCurrency, rearCurrency) match 
		{
			case (null, null) => None
			case (c: String, null) => Some(c)
			case (null,c:String) => Some(c)
			case (c1: String, c2: String) => {
				Logger.error("Found 2 currencies, " + c1 + " and " + c2 + " in \"" + str + "\"")
				Some("???")
			}
		}

		if (noCost != null) return (0.0, currency)

		try {

			val value = NumberFormat.getNumberInstance().parse(amount)

			return (value.doubleValue, currency)

		} catch {
			case pe: ParseException => {
				Logger.error("Couldn't parse number \"" + amount + "\" in \"" + str + "\"")
				return (-99999.0, currency)
			}
		}

	}

	def yesPattern = "[Yy]es".r

	def parseBoolean(boolString: String): Boolean = { yesPattern.pattern.matcher(boolString).matches }


	/**
	A string is either an integer or empty. If it is empty, return 0. Otherwise parse the string as a integer.
	*/
	def parseMaybeInt(str: String): Int = {
		if (str == "" || str == null || str == None) return 0
		else return str.toInt
	}
	
	def parseLine(line: String, fileName: String, lineNum: Int): CSVLine = {

		Logger.debug("\n======================================\n" + line)

		val item = line.split("\t", -1)

		val (aSite, zSite) = parseSites(trimString(item(4), 4))

		val (nrc, nrcCountry) = parseCost(item(13), fileName, lineNum)
		val (mrc, mrcCountry) = parseCost(item(14), fileName, lineNum)
		val (yearlyCost, yearlyCostCountry) = parseCost(item(15), fileName, lineNum)

		val currency = (nrcCountry, mrcCountry) match {
			case (None, None) => "USD"
			case (n, None) => n.get
			case (None, m) => m.get
			case (n, m) => {
				val ng = n.get
				val mg = m.get
				if (ng == mg) ng
				else {
					Logger.error(fileName + ":" + lineNum + " Conflicting currency codes \"" + ng + "\" and \"" + mg + "\" in line \n" + line + "\n")
					ng
				}
			}
		}

		Logger.debug("There are " + item.length + " items")

		CSVLine(
			trimString(item(0), 0),
			trimString(item(1), 1),
			trimString(item(2), 2),
			parseMaybeInt(trimString(item(3), 3)),
			aSite,
			zSite,
			DateOrMTM.parseDate(trimString(item(5), 5)),
			DateOrMTM.parseDate(trimString(item(6), 6)),
			DateOrMTM.parseDate(trimString(item(7), 7)),
			DateOrMTM.parseDate(trimString(item(8), 8)),
			MaybeTerm.parseTerm(trimString(item(9), 9)),
			MaybeTerm.parseTerm(trimString(item(10), 10)),
			trimString(item(11), 11),
			trimString(item(12), 12),
			nrc,
			mrc,
			currency,
			// skipping yearly contract costs
			trimString(item(16), 16),
			trimString(item(17), 17),
			trimString(item(18), 18),
			parseBoolean(trimString(item(19), 19)),
			parseBoolean(trimString(item(20), 20))
		)
	}
}
