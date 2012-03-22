package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current

case class Contact(
	id: Long, 
	name: String, 
	email: String, 
	telephone: Option[String], 
	companyId: Option[Long])

object Contact {
	  
	val contact = {
		get[Long]("id") ~ 
		get[String]("name") ~
		get[String]("email") ~
		get[Option[String]]("telephone") ~
		get[Option[Long]]("company_id") map {
			case id~name~email~telephone~company_id => 
				Contact(id, name, email, telephone, company_id)
		}
	}	

	def contactForm = Form(
		tuple (
			"name" -> nonEmptyText,
			"email" -> nonEmptyText,
			"telephone" -> optional(nonEmptyText),
			"company_id" -> optional(longNumber)
		)
	)

	def findById(id: Long): Option[Contact] = {
		DB.withConnection { implicit c =>
			SQL("select * from contact where id = {id}")
				.on('id -> id)
				.as(Contact.contact.singleOpt)
		}
	}

	def nameById(id: Long): String = {
		val c = findById(id);

		c match {
			case Some(contactObj) => contactObj.name
			case None => "None"
		}
	}


	def all(): List[Contact] = DB.withConnection { implicit c =>
		SQL("select * from contact order by name").as(contact *)
	}
			  
	def create(name: String, email: String, telephone: Option[String], companyId: Option[Long]) {
		DB.withConnection { implicit c =>
			SQL("insert into contact (name, email, telephone, company_id) values ({name}, {email}, {telephone}, {company_id})").on(
				'name -> name,
				'email -> email,
				'telephone -> telephone,
				'company_id -> companyId
			).executeUpdate()
		}
	}
					  
	def delete(id: Long) {
		DB.withConnection { implicit c =>
			SQL("delete from contact where id = {id}").on(
				'id -> id
			).executeUpdate()
		}
	}
	
	// Make Map[String, String] needed for select options in a form.
	def options: Seq[(String, String)] = DB.withConnection { implicit connection => 
		SQL("select * from contact order by name").as(Contact.contact *).map(c => c.id.toString -> (c.name))
	}
	// TODO make the option be name-company
}

