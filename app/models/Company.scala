package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current

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
			  
	def create(name: String, primaryContactId: Option[Long]) {
		DB.withConnection { implicit c =>
			SQL("insert into company (name, primary_contact_id) values ({name}, {primary_contact_id})").on(
				'name -> name,
				'primary_contact_id -> primaryContactId
			).executeUpdate()
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

	def delete(id: Long) {
		DB.withConnection { implicit c =>
			SQL("delete from company where id = {id}").on(
				'id -> id
			).executeUpdate()
		}
	}
	
	// Make Map[String, String] needed for select options in a form.
	def options: Seq[(String, String)] = DB.withConnection { implicit connection => 
		SQL("select * from company order by name").as(Company.company *).map(c => c.id.toString -> (c.name))
	}
}

