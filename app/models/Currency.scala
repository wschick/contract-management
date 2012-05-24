package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import com.mysql.jdbc.exceptions.jdbc4._
import java.sql.SQLException

case class Currency(id: Long, abbreviation: String)

object Currency {
	  
	val currency = {
		get[Long]("id") ~ 
		get[String]("abbreviation") map {
			case id~abbreviation => Currency(id, abbreviation)
		}
	}	

	def all(): List[Currency] = DB.withConnection { implicit c =>
		SQL("select * from currency order by abbreviation").as(currency *)
	}
			  
	def create(abbreviation: String) {
		DB.withConnection { implicit c =>
			SQL("insert into currency (abbreviation) values ({abbreviation})").on(
				'abbreviation -> abbreviation
			).executeUpdate()
		}
	}
					  
	def findById(id: Long): Option[Currency] = {
		DB.withConnection { implicit c =>
			SQL("select * from currency where id = {id}").on('id -> id).as(Currency.currency.singleOpt)
		}
	}

	def findByAbbreviation(abbrev: String): Option[Currency] = {
		DB.withConnection { implicit c =>
			SQL("select * from currency where abbreviation = {abbrev}").on('abbrev -> abbrev).as(Currency.currency.singleOpt)
		}
	}

	def stringById(id: Long): String = {
		val c = findById(id);

		c match {
			case Some(currencyObj) => currencyObj.abbreviation
			case None => "None"
		}
	}

	def update(id: Long, abbreviation: String) {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					update currency set abbreviation={abbreviation} where id={id}
				"""
				).on(
				'id -> id,
				'abbreviation -> abbreviation
			).executeUpdate()
		}
	}
					  

	/**
		Delete a currency

		@passed: id The id of the currency to delete
		@return: None if everything was ok, or a String if the operation failed.
	*/
	def delete(id: Long): Option[String] = {
		try {
			DB.withConnection { implicit c =>
				SQL("delete from currency where id = {id}").on(
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
				Some("Couldn't delete this currency: " + e.getMessage)
		}
	}
	
	// Make Map[String, String] needed for select options in a form.
	def options: Seq[(String, String)] = DB.withConnection { implicit connection => 
		SQL("select * from currency order by abbreviation").as(Currency.currency *).map(c => c.id.toString -> c.abbreviation)
	}
}

// From http://danieldietrich.net/?p=26
