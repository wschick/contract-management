package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current

case class Location(id: Long, code: String, description: String, address: Option[String]) {

	def longString: String = {
		code + " (" + description + ")"
	}

}

object Location {
	  
	val location = {
		get[Long]("id") ~ 
		get[String]("code") ~
		get[String]("description") ~
		get[Option[String]]("address") map {
			case id~code~description~address => Location(id, code, description, address)
		}
	}	

	def findById(id: Long): Option[Location] = {
		DB.withConnection { implicit c =>
			SQL("select * from location where id = {id}").on('id -> id).as(Location.location.singleOpt)
		}
	}

	/** Given location code, find Location object */
	def findByCode(code: String): Option[Location] = {
		DB.withConnection { implicit c =>
			SQL("select * from location where code = {code}").on('code -> code).as(Location.location.singleOpt)
		}
	}

	/** Given an id, return the location code. */
	def codeById(id: Long): String = {
		val c = findById(id);

		c match {
			case Some(locationObj) => locationObj.code
			case None => "None"
		}
	}

	/** Return a list of all locations, ordered by code. */
	def all(): List[Location] = DB.withConnection { implicit c =>
		SQL("select * from location order by code").as(location *)
	}
			  
	/** Create a location from the form submission. */
	def create(code: String, description: String, address: Option[String]) {
		DB.withConnection { implicit c =>
			SQL("insert into location (code, description, address) values ({code}, {description}, {address})").on(
				'code -> code,
				'description -> description,
				'address -> address
			).executeUpdate()
		}
	}

	/** Update a location

		@param id The id of the location object.
		@param code The short code for the location.
		@param description A description of the location.
		
		*/
	def update(id: Long, code: String, description: String, address: Option[String]) {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					update location set code={code}, description={description}, address={address} where id={id}
				"""
				).on(
				'id -> id,
				'code -> code,
				'description -> description,
				'address -> address
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

	
	/** Make Map[String, String] needed for select options in a form. */
	def options: Seq[(String, String)] = DB.withConnection { implicit connection => 
		SQL("select * from location order by code").as(Location.location *).map(c => c.id.toString -> (c.code + " " + c.description))
	}
}

