package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import com.mysql.jdbc.exceptions.jdbc4._
import java.sql.SQLException

case class ContractType(
	id: Pk[Long] = NotAssigned,
	name: String
	) 


object ContractType {

	val contractType = {
		get[Pk[Long]]("id") ~ 
		get[String]("name") map {
			case id~name => ContractType(id, name)
		}
	}	

	/** Get a list of all contracts. */
	def all(): List[ContractType] = DB.withConnection { implicit connection =>
		SQL("select * from contract_type order by name").as(contractType *)
	}

	/** Get a specific contract type.

		@param id the id of the contract type
		@return the contract, if it exists.

		*/
	def findById(id: Long): Option[ContractType] = {
		DB.withConnection { implicit connection =>
			SQL("select * from contract_type where id = {id}").on('id -> id).as(ContractType.contractType.singleOpt)
		}
	}

	/** Get the name of a specific contract type.

		@param id the id of the contract
		@return the contract name, if the contact exists.

		*/
	def nameById(id: Long): Option[String] = {
		findById(id).map(contractType => Some(contractType.name)).getOrElse(None)
	}
			  

	/** Get a contact using its name

		@param name the name of the contract type
		@return the contract, if it exists.

		*/
	def findByName(name: String): Option[ContractType] = {
		DB.withConnection { implicit connection =>
			SQL("select * from contract_type where name = {name}").on('name -> name).as(ContractType.contractType.singleOpt)
		}
	}

	/** Create a contract type in the database.

		@param contractType A ContractType object to be persisted. 
			The unique database key will be provided automatically.
		*/
	def create(contractType: ContractType) = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					insert into contract_type (name) values ({name})
				"""
				).on(
				'name -> contractType.name
			).executeUpdate()
		}
	}

	/** Update a contract type in the database.

		@param contractType A ContractType object to be updated with the values provided 
		*/
	def update(id: Long, contractType: ContractType) = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					update contract_type set name={name} where id={id}
				"""
				).on(
				'id -> id,
				'name -> contractType.name
			).executeUpdate()
		}
	}
					  
	/**
		Delete a contract type

		@passed: id The id of the contract type to delete
		@return: None if everything was ok, or a String if the operation failed.
	*/
	def delete(id: Long): Option[String] = {
		try {
			DB.withConnection { implicit c =>
				SQL("delete from contract_type where id = {id}").on(
					'id -> id
				).executeUpdate()
			}
			return None
		} catch {
			// Sorry this is mysql specific, but I don't think there is a general way to 
			// catch a constraint violation exception.
			case e: MySQLIntegrityConstraintViolationException => 
				Some("Can't delete this because something else depends upon it.")
			case e: SQLException =>
				println(e)
				Some("Couldn't delete this contract type: " + e.getMessage)
		}
	}
							  
	/** Make Map[String, String] needed for contract type select options in a form. 
			This uses the name of the ContractType as the visible text.
		*/
	def options: Seq[(String, String)] = DB.withConnection { 
		implicit connection => 
		SQL("select * from contract_type order by name")
			.as(ContractType.contractType *).map(c => c.id.toString -> (c.name))
	}

}

