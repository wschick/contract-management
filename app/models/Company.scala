package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import com.mysql.jdbc.exceptions.jdbc4._
import java.sql.SQLException

case class Company(
	id: Long, 
	name: String, 
	primaryContactId: Option[Long])

object Company {
	  
	val company = {
		get[Long]("id") ~ 
		get[String]("name") ~
		get[Option[Long]]("primary_contact_id") map {
			case id~name~primary_contact_id => 
				Company(id, name, primary_contact_id)
		}
	}	

	def findById(id: Long): Option[Company] = {
		DB.withConnection { implicit c =>
			SQL("select * from company where id = {id}")
				.on('id -> id)
				.as(Company.company.singleOpt)
		}
	}

	/** Given a name, return a company. This does exact match. */
	def findByName(name: String): Option[Company] = {
		DB.withConnection { implicit c =>
			SQL("select * from company where name = {name}")
				.on('name -> name)
				.as(Company.company.singleOpt)
		}
	}

	def nameById(id: Long): String = {
		val c = findById(id);

		c match {
			case Some(companyObj) => companyObj.name
			case None => ""
		}
	}

	def nameById(id: Option[Long]): String = {
		id match {
			case Some(id) => nameById(id)
			case None => ""
		}
	}

	def all(): List[Company] = DB.withConnection { implicit c =>
		SQL("select * from company order by name").as(company *)
	}
			  
	def create(name: String, primaryContactId: Option[Long]): Long = {
		DB.withConnection { implicit c =>
			SQL("insert into company (name, primary_contact_id) values ({name}, {primary_contact_id})").on(
				'name -> name,
				'primary_contact_id -> primaryContactId
			).executeUpdate()
			return SQL("select LAST_INSERT_ID()").as(scalar[Long].single)
		}
	}
					  
	def update(id: Long, name: String, primaryContactId: Option[Long]) {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					update company set name={name}, primary_contact_id={primary_contact_id} where id={id}
				"""
				).on(
				'id -> id,
				'name -> name,
				'primary_contact_id -> primaryContactId
			).executeUpdate()
		}
	}

	/**
		Delete a company

		@passed: id The id of the company to delete
		@return: None if everything was ok, or a String if the operation failed.
	*/
	def delete(id: Long): Option[String] = {
		try {
			DB.withConnection { implicit c =>
				SQL("delete from company where id = {id}").on(
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
				Some("Couldn't delete this company: " + e.getMessage)
		}
	}
	
	// Make Map[String, String] needed for select options in a form.
	def options: Seq[(String, String)] = DB.withConnection { implicit connection => 
		SQL("select * from company order by name").as(Company.company *).map(c => c.id.toString -> (c.name))
	}
}

