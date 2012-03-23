package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current

case class Location(id: Long, code: String, description: String) {

	def longString: String = {
		code + " (" + description + ")"
	}

}

object Location {
	  
	val location = {
		get[Long]("id") ~ 
		get[String]("code") ~
		get[String]("description") map {
			case id~code~description => Location(id, code, description)
		}
	}	

	def locationForm = Form(
		tuple (
			"code" -> nonEmptyText,
			"description" -> nonEmptyText
		)
	)


	def findById(id: Long): Option[Location] = {
		DB.withConnection { implicit c =>
			SQL("select * from location where id = {id}").on('id -> id).as(Location.location.singleOpt)
		}
	}

	def codeById(id: Long): String = {
		val c = findById(id);

		c match {
			case Some(locationObj) => locationObj.code
			case None => "None"
		}
	}


	def all(): List[Location] = DB.withConnection { implicit c =>
		SQL("select * from location order by code").as(location *)
	}
			  
	def create(code: String, description: String) {
		DB.withConnection { implicit c =>
			SQL("insert into location (label) values ({code}, {description})").on(
				'code -> code,
				'description -> description
			).executeUpdate()
		}
	}
					  
	def delete(id: Long) {
		DB.withConnection { implicit c =>
			SQL("delete from location where id = {id}").on(
				'id -> id
			).executeUpdate()
		}
	}

	def asString(id: Long): String = {
		val c = findById(id);

		c match {
			case Some(locationObj) => locationObj.longString
			case None => "None"
		}
	}

	
	// Make Map[String, String] needed for select options in a form.
	def options: Seq[(String, String)] = DB.withConnection { implicit connection => 
		SQL("select * from location order by code").as(Location.location *).map(c => c.id.toString -> (c.code + " " + c.description))
	}
	//def options: Seq[(String, String)] = DB.withConnection { implicit connection => 
//		SQL("select * from currency order by label").as(Currency.currency *).map(c => c.id.toString -> c.label)
}

