package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current

case class Person(
	id: Long, 
	name: String, 
	email: String, 
	telephone: Option[String], 
	companyId: Long)

object Person {
	  
	val person = {
		get[Long]("id") ~ 
		get[String]("name") ~
		get[String]("email") ~
		get[Option[String]]("telephone") ~
		get[Long]("company_id") map {
			case id~name~email~telephone~company_id => 
				Person(id, name, email, telephone, company_id)
		}
	}	

	def findByName(name: String): Option[Person] = {
		DB.withConnection { implicit c =>
			SQL("select * from person where name = {name}")
				.on('name -> name)
				.as(Person.person.singleOpt)
		}
	}

	def findById(id: Long): Option[Person] = {
		DB.withConnection { implicit c =>
			SQL("select * from person where id = {id}")
				.on('id -> id)
				.as(Person.person.singleOpt)
		}
	}

	def nameById(id: Option[Long]): String = {
		id match {
			case Some(id) => {
				findById(id) match {
					case Some(personObj) => personObj.name
					case None => ""
				}
			}
			case None => ""
		}
	}


	def all(): List[Person] = DB.withConnection { implicit c =>
		SQL("select * from person order by name").as(person *)
	}
			  
	/**
		Create a person

		@return The id of the new person

		*/
	def create(name: String, email: String, telephone: Option[String], companyId: Long): Long = {
		DB.withConnection { implicit c =>
			SQL("insert into person (name, email, telephone, company_id) values ({name}, {email}, {telephone}, {company_id})").on(
				'name -> name,
				'email -> email,
				'telephone -> telephone,
				'company_id -> companyId
			).executeUpdate()
			return SQL("select LAST_INSERT_ID()").as(scalar[Long].single)
		}
	}


	def update(id: Long, name: String, email: String, telephone: Option[String], companyId: Long) {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					update person set name={name}, email={email}, telephone={telephone}, company_id={company_id} where id={id}
				"""
				).on(
				'id -> id,
				'name -> name,
				'email -> email,
				'telephone -> telephone,
				'company_id -> companyId
			).executeUpdate()
		}
	}

	def delete(id: Long) {
		DB.withConnection { implicit c =>
			SQL("delete from person where id = {id}").on(
				'id -> id
			).executeUpdate()
		}
	}
	
	// Make Map[String, String] needed for select options in a form.
	def options: Seq[(String, String)] = DB.withConnection { implicit connection => 
		SQL("select * from person order by name").as(Person.person *)
			.map(p => p.id.toString -> (p.name + " [" + Company.nameById(p.companyId) + "]"))
	}
}

