package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current

case class Currency(id: Long, label: String)

object Currency {
	  
	val currency = {
		get[Long]("id") ~ 
		get[String]("label") map {
			case id~label => Currency(id, label)
		}
	}	

	def currencyForm = Form(
		"label" -> nonEmptyText
	)

	def all(): List[Currency] = DB.withConnection { implicit c =>
		SQL("select * from currency order by label").as(currency *)
	}
			  
	def create(label: String) {
		DB.withConnection { implicit c =>
			SQL("insert into currency (label) values ({label})").on(
				'label -> label
			).executeUpdate()
		}
	}
					  
	def findById(id: Long): Option[Currency] = {
		DB.withConnection { implicit c =>
			SQL("select * from currency where id = {id}").on('id -> id).as(Currency.currency.singleOpt)
		}
	}

	def stringById(id: Long): String = {
		val c = findById(id);

		c match {
			case Some(currencyObj) => currencyObj.label
			case None => "None"
		}
	}

	def delete(id: Long) {
		DB.withConnection { implicit c =>
			SQL("delete from currency where id = {id}").on(
				'id -> id
			).executeUpdate()
		}
	}
	
	// Make Map[String, String] needed for select options in a form.
	def options: Seq[(String, String)] = DB.withConnection { implicit connection => 
		SQL("select * from currency order by label").as(Currency.currency *).map(c => c.id.toString -> c.label)
	}
}

// From http://danieldietrich.net/?p=26
