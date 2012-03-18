package models

import org.joda.time._
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

/*case class Contract(id: Long, company: Company, category: ContractCategory, identifier: String, name: String, 
	description: String, location: ContractLocation, startDate: DateTime, contractTerm: Term, cancellationTerm: Term,
	MRC: BigDecimal, NRC: BigDecimal, currency: Currency, attachments: List[Attachments]
	*/

case class Contract(
	id: Pk[Long] = NotAssigned,
	filingId: String, // Used for filing
	name: String, 
	description: Option[String],
	mrc: Double, 
	nrc: Double,
	currencyId: Long,
	aEndId: Long, 
	zEndId: Long
	)

object Contract {

	val contract = {
		get[Pk[Long]]("id") ~ 
		get[String]("filingId") ~ 
		get[String]("name") ~
		get[Option[String]]("description") ~
		get[Double]("mrc") ~
		get[Double]("nrc") ~
		get[Long]("currency_id") ~
		get[Long]("a_end_id") ~
		get[Long]("z_end_id") map {
			case id~filingId~name~description~mrc~nrc~currencyId~aEndId~zEndId => 
				Contract(id, filingId, name, description, mrc, nrc, currencyId, aEndId, zEndId)
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
			  
	def create(contract: Contract) = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					insert into contract (filingId, name, description, mrc, nrc, currency_id, a_end_id, z_end_id) 
					values ({filingId}, {name}, {description}, {mrc}, {nrc}, {currency_id}, {a_end_id}, {z_end_id})
				"""
				).on(
				'filingId -> contract.filingId,
				'name -> contract.name,
				'description -> contract.description,
				'mrc -> contract.mrc,
				'nrc -> contract.nrc,
				'currency_id -> contract.currencyId,
				'a_end_id -> contract.aEndId,
				'z_end_id -> contract.zEndId
			).executeUpdate()
		}
	}
					  
	def delete(id: Long) {
		DB.withConnection { implicit connection =>
			SQL("delete from contract where id = {id}").on(
				'id -> id).executeUpdate()
		}
	}
							  

}

