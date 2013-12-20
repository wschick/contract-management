package models

import anorm._
import anorm.SqlParser._
import play.api._
import play.api.db._
import play.api.Play.current
import org.joda.time._
import java.util.Date

case class Contract(
	id: Pk[Long] = NotAssigned,
	vendor: Company,
	vendorContractId: String, // Used for filing
	billingAccount: Option[String],
	isMSA: Boolean,
	MSAId: Option[Long], // Points to contract that is MSA.
	extraInfo: Option[String], 
	description: Option[String],
	contractType: ContractType,
	aEnd: Location, 
	zEnd: Location,
	cost: ContractCosts,
	startDate: LocalDate,
	term: Term,
	cancellationPeriod: Term,
	cancelledDate: Option[LocalDate],
	autoRenewPeriod: Option[Term],
	attention: Option[String],
	lastModifyingUser: Option[String] = None,
	lastModifiedTime: Option[LocalDateTime] = Some(LocalDateTime.now())
	) 
{

	/**
		@returns company name and vendor's contract id, concatenated
	*/
	def vendorIdString(): String = {
		Company.findById(vendor.id).get.name + " " + vendorContractId
	}

	def name(): String = vendorIdString

	def startDateStr(): String = DateUtil.format(startDate)

	def cancelledDateStr(): String = cancelledDate.map(cd => DateUtil.format(cd)).getOrElse("")

	def lastDay(): LocalDate = DateUtil.calculateLastDay(startDate, term, autoRenewPeriod)

	def lastDayStr(): String = DateUtil.format(lastDay)

	def cancellationDate(): LocalDate = {
		lastDay.minus(cancellationPeriod.period)
	}

	def cancellationDateStr(): String = DateUtil.format(cancellationDate)

	def daysUntilCancellationDate(): Int = {
		Days.daysBetween(new LocalDate(), cancellationDate()).getDays
	}

	def willAutoRenew(): Boolean = autoRenewPeriod != None

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

	def hasAttachments: Boolean = Attachment.contractHasAttachments(Company.findById(vendor.id).get.name, vendorContractId)

	def attachments(): Seq[Attachment] = Attachment.getContractAttachments(Company.findById(vendor.id).get.name, vendorContractId)

	def lastModifiedTimeStr(): String = DateUtil.formatDT(lastModifiedTime)
}

object Contract {

	val contract = {
		get[Pk[Long]]("id") ~ 
		get[Long]("vendor_id") ~
		get[String]("vendor_contract_id") ~ 
		get[Option[String]]("billing_account") ~ 
		get[Boolean]("is_msa") ~
		get[Option[Long]]("msa_id") ~
		get[Option[String]]("extra_info") ~
		get[Option[String]]("description") ~
		get[Long]("contract_type_id") ~
		get[Long]("a_end_id") ~
		get[Long]("z_end_id") ~
		get[Double]("mrc") ~
		get[Double]("nrc") ~
		get[Long]("currency_id") ~
		get[Long]("budget_id") ~
		get[Date]("start_date") ~
		get[Int]("term") ~
		get[Int]("term_units") ~
		get[Int]("cancellation_period") ~
		get[Int]("cancellation_period_units") ~
		get[Option[Date]]("cancelled_date") ~
		get[Option[Int]]("auto_renew_period") ~
		get[Option[Int]]("auto_renew_period_units") ~
		get[Option[String]]("attention") ~
		get[Option[String]]("last_modifying_user") ~
		get[Option[Date]]("last_modified_time") map {
			case id~vendorId~vendorContractId~billingAccount~isMSA~msa_id~
				extraInfo~description~contractTypeId~aEndId~zEndId~
				mrc~nrc~currencyId~budgetId~
				startDate~term~termUnits~
				cancellationPeriod~cancellationPeriodUnits~cancelledDate~
				autoRenewPeriod~autoRenewPeriodUnits~
				attention~
				lastModifyingUser~lastModifiedTime => 
				Contract(id, Company.findById(vendorId).get, vendorContractId, billingAccount, isMSA, msa_id, 
					extraInfo, description, ContractType.findById(contractTypeId).get, 
					Location.findById(aEndId).get, Location.findById(zEndId).get, 
					ContractCosts.create(mrc, nrc, currencyId, budgetId),
					new LocalDate(startDate), 
					Term(term, TimePeriodUnits.create(termUnits)), 
					Term(cancellationPeriod, TimePeriodUnits.create(cancellationPeriodUnits)), 
					cancelledDate.map(date => Option(new LocalDate(date))).getOrElse(None),
					{ if (autoRenewPeriod == None || autoRenewPeriodUnits == None) None
						else Some(Term(autoRenewPeriod.get, TimePeriodUnits.create(autoRenewPeriodUnits.get))) }, 
					attention, 
					lastModifyingUser, 
					lastModifiedTime.map(lmt => Some(new LocalDateTime(lmt))).getOrElse(None)
					)
				//TODO this will blow up if it can't find the contract type or budget
		}
	}	

	/** Get a list of all contracts. */
	def all(): List[Contract] = DB.withConnection { implicit connection =>
		SQL("select * from contract").as(contract *)
	}

	def filtered(filter: ContractFilter): List[Contract] = {
		DB.withConnection { implicit connection =>
      Logger.debug("My SQL string: " + "select * from contract " + filter.sqlCondition)
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
							vendor_id, vendor_contract_id, billing_account, is_msa, msa_id,
							extra_info, description, contract_type_id, a_end_id, z_end_id, 
							mrc, nrc, currency_id, budget_id, 
							start_date, term, term_units, 
							cancellation_period, cancellation_period_units, cancelled_date,
							auto_renew_period, auto_renew_period_units,
							attention,
							last_modifying_user, last_modified_time
							) 
						values (
							{vendorId}, {vendorContractId}, {billing_account}, {is_msa}, {msa_id},
							{extra_info}, {description}, {contract_type_id}, {a_end_id}, {z_end_id}, 
							{mrc}, {nrc}, {currency_id}, {budget_id}, 
							{start_date}, {term}, {term_units},
							{cancellation_period}, {cancellation_period_units}, {cancelled_date},
							{auto_renew_period}, {auto_renew_period_units},
							{attention},
							{last_modifying_user}, {last_modified_time} )
					"""
					).on(
					'vendorId -> contract.vendor.id,
					'vendorContractId -> contract.vendorContractId,
					'billing_account -> contract.billingAccount,
					'is_msa -> contract.isMSA,
					'msa_id -> contract.MSAId,
					'extra_info -> contract.extraInfo,
					'description -> contract.description,
					'contract_type_id -> contract.contractType.id,
					'mrc -> contract.cost.mrc,
					'nrc -> contract.cost.nrc,
					'currency_id -> contract.cost.currency.id,
					'budget_id -> contract.cost.budget.id,
					'a_end_id -> contract.aEnd.id,
					'z_end_id -> contract.zEnd.id,
					'start_date -> contract.startDate.toDate,
					'term -> contract.term.length,
					'term_units -> contract.term.units.value,
					'cancellation_period -> contract.cancellationPeriod.length,
					'cancellation_period_units -> contract.cancellationPeriod.units.value,
					'cancelled_date -> contract.cancelledDate.map(date => date.toDate),
					'auto_renew_period -> contract.autoRenewPeriod.map(arp => arp.length),
					'auto_renew_period_units -> contract.autoRenewPeriod.map(arp => arp.units.value),
					'attention -> contract.attention,
					'last_modifying_user -> contract.lastModifyingUser,
					'last_modified_time -> contract.lastModifiedTime.map(lmt => lmt.toDate)
				).executeUpdate()
				return SQL("select LAST_INSERT_ID()").as(scalar[Long].single)
			}
		}
	}

	def update(id: Long, contract: Contract) {
		Logger.debug("updating id " + id)
		DB.withConnection { implicit connection =>
				SQL(
				"""
					update contract set 
					vendor_id={vendorId}, vendor_contract_id={vendorContractId}, billing_account={billingAccount}, 
					is_msa={is_msa}, msa_id={msa_id},
					extra_info={extra_info}, description={description}, contract_type_id={contract_type_id}, 
					a_end_id={a_end_id}, z_end_id={z_end_id}, 
					mrc={mrc}, nrc={nrc}, currency_id={currency_id}, budget_id={budget_id}, 
					start_date={start_date}, term={term}, term_units={term_units},
					cancellation_period={cancellation_period}, cancellation_period_units={cancellation_period_units}, 
					cancelled_date={cancelled_date}, 
					auto_renew_period={auto_renew_period}, auto_renew_period_units={auto_renew_period_units},
					attention={attention}, 
					last_modifying_user={last_modifying_user}, last_modified_time={last_modified_time} 
					where id={id}
				"""
				).on(
				'id -> id,
				'vendorId -> contract.vendor.id,
				'vendorContractId -> contract.vendorContractId,
				'billingAccount -> contract.billingAccount,
				'is_msa -> contract.isMSA,
				'msa_id -> contract.MSAId,
				'extra_info -> contract.extraInfo,
				'description -> contract.description,
				'contract_type_id -> contract.contractType.id,
				'a_end_id -> contract.aEnd.id,
				'z_end_id -> contract.zEnd.id,
				'mrc -> contract.cost.mrc,
				'nrc -> contract.cost.nrc,
				'currency_id -> contract.cost.currency.id,
				'budget_id -> contract.cost.budget.id,
				'start_date -> contract.startDate.toDate,
				'term -> contract.term.length,
				'term_units -> contract.term.units.value,
				'cancellation_period -> contract.cancellationPeriod.length,
				'cancellation_period_units -> contract.cancellationPeriod.units.value,
				'cancelled_date -> contract.cancelledDate.map(date => date.toDate),
				'auto_renew_period -> contract.autoRenewPeriod.map(arp => arp.length),
				'auto_renew_period_units -> contract.autoRenewPeriod.map(arp => arp.units.value),
				'attention -> contract.attention,
				'last_modifying_user -> contract.lastModifyingUser,
				'last_modified_time -> contract.lastModifiedTime.map(lmt => lmt.toDate)
				).executeUpdate()
		}
	}
					  
	/** Delete a contract

		@param id the id of the contract

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
	def options(msaOnly: Boolean = false): Seq[(String, String)] = DB.withConnection { implicit connection => 
		val select = SQL("select c.id,v.name,c.vendor_contract_id from contract c inner join company v " +
			"where c.vendor_id=v.id " + 
			//{if (msaOnly) "and c.is_msa=1 "; else ""} +
			{msaOnly match { case true => "and c.is_msa=1 "; case false => " "}} +
			"order by v.name,c.vendor_contract_id")
		select().map(row => row[Int]("id").toString -> (row[String]("name") + " " + row[String]("vendor_contract_id"))).toList
	}

	/** Make Map[String, String] needed for MSA select options in a form. 
			This uses the name of the contract as the visible text.
		*/
	def MSAOptions: Seq[(String, String)] = options(true)

}
