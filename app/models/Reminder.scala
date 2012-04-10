package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import java.util.{Date}
import org.joda.time._

case class ReminderAndPeople(reminder: Reminder, people: List[Long]);

case class Reminder(
	id: Pk[Long], 
	reminderDate: LocalDate, 
	contractId: Long, 
	sent: Boolean)

object Reminder {
	  
	val reminder = {
		get[Pk[Long]]("id") ~ 
		get[Date]("reminder_date") ~
		get[Long]("contract_id") ~
		get[Boolean]("sent") map {
			case id~reminder_date~contract_id~sent => 
				Reminder(id, new LocalDate(reminder_date), contract_id, sent)
		}
	}	

	def findById(id: Long): Option[Reminder] = {
		DB.withConnection { implicit c =>
			SQL("select * from reminder where id = {id}")
				.on('id -> id)
				.as(Reminder.reminder.singleOpt)
		}
	}

	def reminderDateById(id: Long): Option[LocalDate] = {
		val c = findById(id);

		c match {
			case Some(reminderObj) => Some(reminderObj.reminderDate)
			case None => None
		}
	}


 val personName = {
	 get[String]("name") map {
		 case name => name
	 }
 }

	def personNamesForReminder(reminderId: Pk[Long], maxPersons: Int = 1): List[String] = {
		DB.withConnection { implicit c =>
			SQL("select person.name from reminder INNER JOIN reminder_person INNER JOIN person where reminder.id = {id} AND reminder_person.reminder_id = reminder.id AND reminder_person.person_id = person.id LIMIT 0," + maxPersons)
				.on('id -> reminderId).as(personName *)
		}
	}




	def all(): List[Reminder] = DB.withConnection { implicit c =>
		SQL("select * from reminder order by reminder_date").as(reminder *)
	}
			  
	/*def create(reminder: Reminder) {
		println("The reminder date is " + formatDate(reminder.reminderDate))
		create(new LocalDate(reminder.reminderDate), contractId)
	}*/

	def create(reminder: Reminder) {
		DB.withConnection { implicit c =>
			SQL("insert into reminder (reminder_date, contract_id) values ({reminder_date}, {contract_id})").on(
				'reminder_date -> reminder.reminderDate.toString,
				'contract_id -> reminder.contractId
			).executeUpdate()
		}
	}

	/*def update(id: Long, reminderDate: Date, contractId: Long) {
		update(id, new LocalDate(reminderDate), contractId)
	}*/

	//def update(id: Long, reminderDate: LocalDate, contractId: Long) {
	def update(id: Long, reminder: Reminder) {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					update reminder set reminder_date={reminder_date}, contract_id={contract_id}, sent={sent} where id={id}
				"""
				).on(
				'id -> id,
				'reminder_date -> reminder.reminderDate.toString,
				'contract_id -> reminder.contractId,
				'sent -> reminder.sent
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
			.map(c => c.id.toString -> c.reminderDate.toString())
			//.map(c => c.id.toString -> (formatDate(c.reminderDate)))
	}
}

