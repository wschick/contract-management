package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
//import org.joda.time._
import java.util.Date

/*case class Contract(id: Long, company: Company, category: ContractCategory, identifier: String, name: String, 
	description: String, location: ContractLocation, startDate: DateTime, contractTerm: Term, cancellationTerm: Term,
	MRC: BigDecimal, NRC: BigDecimal, currency: Currency, attachments: List[Attachments]
	*/

case class Contract(
	id: Pk[Long] = NotAssigned,
	contractId: String, // Used for filing
	name: String, 
	description: Option[String],
	mrc: Double, 
	nrc: Double,
	currencyId: Long,
	aEndId: Long, 
	zEndId: Long,
	//startDate: Date,
	term: Int,
	termUnits: Int,
	cancellationPeriod: Int,
	cancellationPeriodUnits: Int,
	reminderPeriod: Option[Int],
	reminderPeriodUnits: Option[Int],
	lastModifyingUser: Option[String],
	lastModifiedTime: Option[Date],
	companyId: Long
	)

object Contract {

	val contract = {
		get[Pk[Long]]("id") ~ 
		get[String]("contract_id") ~ 
		get[String]("name") ~
		get[Option[String]]("description") ~
		get[Double]("mrc") ~
		get[Double]("nrc") ~
		get[Long]("currency_id") ~
		get[Long]("a_end_id") ~
		get[Long]("z_end_id") ~
		//get[Date]("start_date") ~
		get[Int]("term") ~
		get[Int]("term_units") ~
		get[Int]("cancellation_period") ~
		get[Int]("cancellation_period_units") ~
		get[Option[Int]]("reminder_period") ~
		get[Option[Int]]("reminder_period_units") ~
		get[Option[String]]("last_modifying_user") ~
		get[Option[Date]]("last_modified_time") ~
		get[Long]("company_id") map {
			case id~contractId~name~description~mrc~nrc~currencyId~aEndId~zEndId~term~termUnits~cancellationPeriod~cancellationPeriodUnits~reminderPeriod~reminderPeriodUnits~lastModifyingUser~lastModifiedTime~companyId => 
				Contract(id, contractId, name, description, mrc, nrc, currencyId,
					aEndId, zEndId, /*startDate,*/ term, termUnits, cancellationPeriod, 
					cancellationPeriodUnits, reminderPeriod, reminderPeriodUnits,
					lastModifyingUser, lastModifiedTime, companyId)
		}
	}	

	def all(): List[Contract] = DB.withConnection { implicit connection =>
		SQL("select * from contract").as(contract *)
	}

	def findById(id: Long): Option[Contract] = {
		DB.withConnection { implicit connection =>
			SQL("select * from contract where id = {id}").on('id -> id).as(Contract.contract.singleOpt)
		}
	}

	def nameById(id: Long): Option[String] = {
		findById(id).map(contract => Some(contract.name)).getOrElse(None)
	}
			  
	def create(contract: Contract) = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					insert into contract (contract_id, name, description, mrc, nrc, 
					currency_id, a_end_id, z_end_id, term, term_units, 
					cancellation_period, cancellation_period_units,
					reminder_period, reminder_period_units, 
					last_modifying_user, last_modified_time) 
					values ({contractId}, {name}, {description}, {mrc}, {nrc}, 
					{currency_id}, {a_end_id}, {z_end_id}, {term}, {term_units},
					{cancellation_period}, {cancellation_period_units}, 
					{reminder_period}, {reminder_period_units},
					{last_modifying_user}, {last_modified_time})
				"""
				).on(
				'contractId -> contract.contractId,
				'name -> contract.name,
				'description -> contract.description,
				'mrc -> contract.mrc,
				'nrc -> contract.nrc,
				'currency_id -> contract.currencyId,
				'a_end_id -> contract.aEndId,
				'z_end_id -> contract.zEndId,
				//'start_date -> contract.startDate,
				'term -> contract.term,
				'term_units -> contract.termUnits,
				'cancellation_period -> contract.cancellationPeriod,
				'cancellation_period_units -> contract.cancellationPeriodUnits,
				'reminder_period -> contract.reminderPeriod,
				'reminder_period_units -> contract.reminderPeriodUnits,
				'last_modifying_user -> "unknown user",
				'last_modified_time -> new Date
			).executeUpdate()
		}
	}
					  
	def delete(id: Long) {
		DB.withConnection { implicit connection =>
			SQL("delete from contract where id = {id}").on(
				'id -> id).executeUpdate()
		}
	}
							  
	// Make Map[String, String] needed for select options in a form.
	def options: Seq[(String, String)] = DB.withConnection { implicit connection => 
		SQL("select * from contract order by name").as(Contract.contract *).map(c => c.id.toString -> (c.name))
	}

}

