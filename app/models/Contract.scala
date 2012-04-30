package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import org.joda.time._
import java.util.Date

case class Contract(
	id: Pk[Long] = NotAssigned,
	vendorContractId: String, // Used for filing
	billingAccount: Option[String],
	name: String, 
	description: Option[String],
	mrc: Double, 
	nrc: Double,
	currencyId: Long,
	aEnd: Location, 
	zEnd: Location,
	startDate: LocalDate,
	term: Term,
	cancellationPeriod: Term,
	cancelledDate: Option[LocalDate],
	lastModifyingUser: Option[String],
	lastModifiedTime: Option[Date],
	companyId: Long,
	//company: Company,
	contractType: ContractType,
	attention: Option[String],
	budget: Budget,
	isMSA: Boolean,
	MSAId: Option[Long] // Points to contract that is MSA.
	) {

	/**
		@returns company name and vendor's contract id, concatenated
	*/
	def vendorIdString(): String = {
		Company.findById(companyId).get.name + " " + vendorContractId
	}

	def lastDay(): LocalDate = {
		//val jSD = new DateTime(startDate)
		val endDate = startDate.plus(term.period).minus(Days.ONE)
		endDate
	}

	def cancellationDate(): LocalDate = {
		lastDay.minus(cancellationPeriod.period)
	}

	def daysUntilCancellationDate(): Int = {
		Days.daysBetween(new LocalDate(), cancellationDate()).getDays

	}

	def status(): ContractStatus = {
		cancelledDate match {
			case Some(x) => CANCELLED
			case None => {
				daysUntilCancellationDate match {
					case x if (x <= 0) => TOOLATE
					case x if (x < 30) => NEARWARNING
					case x if (x < 60) => FARWARNING
					case _ => OK
				}
			}
		}
	}

	def hasAttachments: Boolean = Attachment.contractHasAttachments(Company.findById(companyId).get.name, vendorContractId)

	def attachments(): Seq[Attachment] = Attachment.getContractAttachments(Company.findById(companyId).get.name, vendorContractId)

}


object Contract {

	val contract = {
		get[Pk[Long]]("id") ~ 
		get[String]("vendor_contract_id") ~ 
		get[Option[String]]("billing_account") ~ 
		get[String]("name") ~
		get[Option[String]]("description") ~
		get[Double]("mrc") ~
		get[Double]("nrc") ~
		get[Long]("currency_id") ~
		get[Long]("a_end_id") ~
		get[Long]("z_end_id") ~
		get[Date]("start_date") ~
		get[Int]("term") ~
		get[Int]("term_units") ~
		get[Int]("cancellation_period") ~
		get[Int]("cancellation_period_units") ~
		get[Option[Date]]("cancelled_date") ~
		get[Option[String]]("last_modifying_user") ~
		get[Option[Date]]("last_modified_time") ~
		get[Long]("company_id") ~
		get[Long]("contract_type_id") ~
		get[Option[String]]("attention") ~
		get[Long]("budget_id") ~
		get[Boolean]("is_msa") ~
		get[Option[Long]]("msa_id") map {
			case id~vendorContractId~billingAccount~name~description~mrc~nrc~currencyId~
				aEndId~zEndId~startDate~term~termUnits~cancellationPeriod~cancellationPeriodUnits~
				cancelledDate~lastModifyingUser~lastModifiedTime~companyId~contractTypeId~
				attention~budgetId~isMSA~msa_id => 
				Contract(id, vendorContractId, billingAccount, name, description, mrc, nrc, currencyId,
					Location.findById(aEndId).get, Location.findById(zEndId).get, 
					new LocalDate(startDate), 
					Term(term, TimePeriodUnits.create(termUnits)), 
					Term(cancellationPeriod, TimePeriodUnits.create(cancellationPeriodUnits)), 
					cancelledDate.map(date => Option(new LocalDate(date))).getOrElse(None),
					lastModifyingUser, lastModifiedTime, companyId, ContractType.findById(contractTypeId).get, 
					attention, Budget.findById(budgetId).get, isMSA, msa_id)
				//TODO this will blow up if it can't find the contract type or budget
		}
	}	

	/** Get a list of all contracts. */
	def all(): List[Contract] = DB.withConnection { implicit connection =>
		SQL("select * from contract").as(contract *)
	}

	def filtered(filter: ContractFilter): List[Contract] = {
		DB.withConnection { implicit connection =>
			SQL("select * from contract " + filter.sqlCondition).as(contract *)
		}// and do something here to let the contract filter pick which ones are kept. Have filter method on contract filter object.
	}

	/** Return a contract.

		@param id the id of the contract
		@return the contract, if it exists.

		*/
	def findById(id: Long): Option[Contract] = {
		DB.withConnection { implicit connection =>
			SQL("select * from contract where id = {id}").on('id -> id).as(Contract.contract.singleOpt)
		}
	}

	/** Return a contract.

		@param vendorContractId the textual id of the contract (not the numerical database id)
		@return the contract, if it exists.

		*/
	def findByVendorContractId(companyId: Long, vendorContractId: String): Option[Contract] = {
		DB.withConnection { implicit connection =>
			SQL("select * from contract where vendor_contract_id={vendor_contract_id} and company_id={company_id}")
				.on('vendor_contract_id -> vendorContractId, 'company_id -> companyId).
				as(Contract.contract.singleOpt)
		}
	}

	/** Return the name of a contract.

		@param id the id of the contract
		@return the contract name, if the contact exists.

		*/
	def nameById(id: Long): Option[String] = {
		findById(id).map(contract => Some(contract.name)).getOrElse(None)
	}
			  
	/** Create a contract in the database.

		@param contract A contract object to be persisted. The unique database key will be provided automatically.
		@return The id of the contract just created.
		*/
	def create(contract: Contract): Long = {
		DB.withConnection { implicit connection =>
			{
				SQL(
					"""
						insert into contract (
							vendor_contract_id, billing_account, name, description, mrc, nrc, 
							currency_id, a_end_id, z_end_id, start_date, term, term_units, 
							cancellation_period, cancellation_period_units, cancelled_date,
							last_modifying_user, last_modified_time, company_id, contract_type_id, attention,
							budget_id, is_mas, msa_id) 
						values (
							{vendorContractId}, {billing_account}, {name}, {description}, {mrc}, {nrc}, 
							{currency_id}, {a_end_id}, {z_end_id}, {start_date}, {term}, {term_units},
							{cancellation_period}, {cancellation_period_units}, {cancelled_date},
							{last_modifying_user}, {last_modified_time}, {companyId}, {contract_type_id}, {attention},
							{budget_id}, {is_msa}, {msa_id})
					"""
					).on(
					'vendorContractId -> contract.vendorContractId,
					'billing_account -> contract.billingAccount,
					'name -> contract.name,
					'description -> contract.description,
					'mrc -> contract.mrc,
					'nrc -> contract.nrc,
					'currency_id -> contract.currencyId,
					'a_end_id -> contract.aEnd.id,
					'z_end_id -> contract.zEnd.id,
					'start_date -> contract.startDate.toDate,
					'term -> contract.term.length,
					'term_units -> contract.term.units.value,
					'cancellation_period -> contract.cancellationPeriod.length,
					'cancellation_period_units -> contract.cancellationPeriod.units.value,
					'cancelled_date -> contract.cancelledDate.map(date => date.toDate).getOrElse(None),
					'last_modifying_user -> "unknown user",
					'last_modified_time -> new Date,
					'companyId -> contract.companyId,
					'contract_type_id -> contract.contractType.id,
					'attention -> contract.attention,
					'budget_id -> contract.budget.id,
					'is_msa -> contract.isMSA,
					'msa_id -> contract.MSAId
				).executeUpdate()
				return SQL("select LAST_INSERT_ID()").as(scalar[Long].single)
			}
		}
	}

	def update(id: Long, contract: Contract) {
		println("updating id " + id)
		DB.withConnection { implicit connection =>
				SQL(
				"""
					update contract set vendor_contract_id={vendorContractId}, billing_account={billingAccount}, 
					name={name}, description={description}, 
					mrc={mrc}, nrc={nrc}, currency_id={currency_id}, a_end_id={a_end_id}, z_end_id={z_end_id}, 
					start_date={start_date}, term={term}, term_units={term_units},
					cancellation_period={cancellation_period}, cancellation_period_units={cancellation_period_units}, 
					cancelled_date={cancelled_date}, last_modifying_user={last_modifying_user}, 
					last_modified_time={last_modified_time}, company_id={companyId}, contract_type_id={contract_type_id}, 
					attention={attention}, budget_id={budget_id}, is_msa={is_msa}, msa_id={msa_id} where id={id}
				"""
				).on(
				'id -> id,
				'vendorContractId -> contract.vendorContractId,
				'billingAccount -> contract.billingAccount,
				'name -> contract.name,
				'description -> contract.description,
				'mrc -> contract.mrc,
				'nrc -> contract.nrc,
				'currency_id -> contract.currencyId,
				'a_end_id -> contract.aEnd.id,
				'z_end_id -> contract.zEnd.id,
				'start_date -> contract.startDate.toDate,
				'term -> contract.term.length,
				'term_units -> contract.term.units.value,
				'cancellation_period -> contract.cancellationPeriod.length,
				'cancellation_period_units -> contract.cancellationPeriod.units.value,
				'cancelled_date -> contract.cancelledDate.map(date => date.toDate).getOrElse(None),
				'last_modifying_user -> "unknown user",
				'last_modified_time -> new Date,
				'companyId -> contract.companyId,
				'contract_type_id -> contract.contractType.id,
				'attention -> contract.attention,
				'budget_id -> contract.budget.id,
				'is_msa -> contract.isMSA,
				'msa_id -> contract.MSAId
				).executeUpdate()
		}
	}
					  
	/** Delete a contract

		@param id the id of the contract
		@return the contract name, if the contact exists.

		*/
	def delete(id: Long) {
		DB.withConnection { implicit connection =>
			SQL("delete from contract where id = {id}").on(
				'id -> id).executeUpdate()
		}
	}
							  
	/** Make Map[String, String] needed for contract select options in a form. 
			This uses the name of the contract as the visible text.
		*/
	def options: Seq[(String, String)] = DB.withConnection { implicit connection => 
		SQL("select * from contract order by name").as(Contract.contract *).map(c => c.id.toString -> (c.name))
	}

	/** Make Map[String, String] needed for MSA select options in a form. 
			This uses the name of the contract as the visible text.
		*/
	def MSAOptions: Seq[(String, String)] = DB.withConnection { implicit connection => 
		SQL("select * from contract where is_msa=1 order by name").as(Contract.contract *).map(c => c.id.toString -> (c.name))
	}

}

