package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import java.util.{Date}

case class Reminder(
	id: Long, 
	reminderDate: Date, 
	contractId: Long)

object Reminder {
	  
	val reminder = {
		get[Long]("id") ~ 
		get[Date]("reminder_date") ~
		get[Long]("contract_id") map {
			case id~reminder_date~contract_id => 
				Reminder(id, reminder_date, contract_id)
		}
	}	

	def reminderForm = Form(
		tuple (
			"reminder_date" -> nonEmptyText,
			"contract_id" -> longNumber
		)
	)

	def findById(id: Long): Option[Reminder] = {
		DB.withConnection { implicit c =>
			SQL("select * from reminder where id = {id}")
				.on('id -> id)
				.as(Reminder.reminder.singleOpt)
		}
	}

	def reminderDateById(id: Long): Option[Date] = {
		val c = findById(id);

		c match {
			case Some(reminderObj) => Some(reminderObj.reminderDate)
			case None => None
		}
	}


	def all(): List[Reminder] = DB.withConnection { implicit c =>
		SQL("select * from reminder order by reminder_date").as(reminder *)
	}
			  
	def create(reminderDate: String, contractId: Long) {
		DB.withConnection { implicit c =>
			SQL("insert into reminder (reminder_date, contract_id) values ({reminder_date}, {contract_id})").on(
				'reminder_date -> reminderDate,
				'contract_id -> contractId
			).executeUpdate()
		}
	}
					  
	def delete(id: Long) {
		DB.withConnection { implicit c =>
			SQL("delete from reminder where id = {id}").on(
				'id -> id
			).executeUpdate()
		}
	}
	
	val dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd")

	def formatDate(d: Date): String = {
		dateFormatter.format(d)
	}

	// Make Map[String, String] needed for select options in a form.
	def options: Seq[(String, String)] = DB.withConnection { implicit connection => 
		SQL("select * from reminder order by reminderDate")
			.as(Reminder.reminder *)
			.map(c => c.id.toString -> (formatDate(c.reminderDate)))
	}
}

