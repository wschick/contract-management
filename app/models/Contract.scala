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
	companyId: Long,
	vendorContractId: String, // Used for filing
	billingAccount: Option[String],
	isMSA: Boolean,
	MSAId: Option[Long], // Points to contract that is MSA.
	name: String, 
	description: Option[String],
	contractType: ContractType,
	aEnd: Location, 
	zEnd: Location,
	cost: ContractCosts,
	/*mrc: Double, 
	nrc: Double,
	currencyId: Long,
	budget: Budget,*/
	startDate: LocalDate,
	term: Term,
	cancellationPeriod: Term,
	cancelledDate: Option[LocalDate],
	autoRenewPeriod: Option[Term],
	attention: Option[String],
	lastModifyingUser: Option[String] = None,
	lastModifiedTime: Option[LocalDate] = Some(LocalDate.now())
	) 
{

	/**
		@returns company name and vendor's contract id, concatenated
	*/
	def vendorIdString(): String = {
		Company.findById(companyId).get.name + " " + vendorContractId
	}

	def lastDay(): LocalDate = DateUtil.calculateLastDay(startDate, term, autoRenewPeriod)

	def cancellationDate(): LocalDate = {
		lastDay.minus(cancellationPeriod.period)
	}

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

	def hasAttachments: Boolean = Attachment.contractHasAttachments(Company.findById(companyId).get.name, vendorContractId)

	def attachments(): Seq[Attachment] = Attachment.getContractAttachments(Company.findById(companyId).get.name, vendorContractId)

}

object Contract {

	val contract = {
		get[Pk[Long]]("id") ~ 
		get[Long]("company_id") ~
		get[String]("vendor_contract_id") ~ 
		get[Option[String]]("billing_account") ~ 
		get[Boolean]("is_msa") ~
		get[Option[Long]]("msa_id") ~
		get[String]("name") ~
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
			case id~companyId~vendorContractId~billingAccount~isMSA~msa_id~
				name~description~contractTypeId~aEndId~zEndId~
				mrc~nrc~currencyId~budgetId~
				startDate~term~termUnits~
				cancellationPeriod~cancellationPeriodUnits~cancelledDate~
				autoRenewPeriod~autoRenewPeriodUnits~
				attention~
				lastModifyingUser~lastModifiedTime => 
				Contract(id, companyId, vendorContractId, billingAccount, isMSA, msa_id, 
					name, description, ContractType.findById(contractTypeId).get, 
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
					lastModifiedTime.map(lmt => Some(new LocalDate(lmt))).getOrElse(None)
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
							company_id, vendor_contract_id, billing_account, is_msa, msa_id
							name, description, contract_type_id, a_end_id, z_end_id, 
							mrc, nrc, currency_id, budget_id, 
							start_date, term, term_units, 
							cancellation_period, cancellation_period_units, cancelled_date,
							auto_renew_period, auto_renew_period_units,
							attention,
							last_modifying_user, last_modified_time
							) 
						values (
							{companyId}, {vendorContractId}, {billing_account}, {is_msa}, {msa_id},
							{name}, {description}, {contract_type_id}, {a_end_id}, {z_end_id}, 
							{mrc}, {nrc}, {currency_id}, {budget_id}, 
							{start_date}, {term}, {term_units},
							{cancellation_period}, {cancellation_period_units}, {cancelled_date},
							{auto_renew_period}, {auto_renew_period_units},
							{attention},
							{last_modifying_user}, {last_modified_time} )
					"""
					).on(
					'companyId -> contract.companyId,
					'vendorContractId -> contract.vendorContractId,
					'billing_account -> contract.billingAccount,
					'is_msa -> contract.isMSA,
					'msa_id -> contract.MSAId,
					'name -> contract.name,
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
					'last_modifying_user -> "unknown user",
					'last_modified_time -> new Date()
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
					company_id={companyId}, vendor_contract_id={vendorContractId}, billing_account={billingAccount}, 
					is_msa={is_msa}, msa_id={msa_id},
					name={name}, description={description}, contract_type_id={contract_type_id}, 
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
				'companyId -> contract.companyId,
				'vendorContractId -> contract.vendorContractId,
				'billingAccount -> contract.billingAccount,
				'is_msa -> contract.isMSA,
				'msa_id -> contract.MSAId,
				'name -> contract.name,
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
				'last_modifying_user -> "unknown user",
				'last_modified_time -> new Date
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
