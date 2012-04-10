package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current

case class ReminderPerson(reminderId: Long, personId: Long) {

}

object ReminderPerson {
	  
	val reminderPerson = {
		get[Long]("reminder_id") ~ 
		get[Long]("person_id") map {
			case reminder_id~person_id => ReminderPerson(reminder_id, person_id)
		}
	}	

	def findByReminderId(id: Long): List[ReminderPerson] = {
		DB.withConnection { implicit c =>
			SQL("select * from reminder_person where reminder_id = {id}").on('id -> id).as(reminderPerson *)
		}
	}

	/** Return a list of all reminder-person entries. */
	def all(): List[ReminderPerson] = DB.withConnection { implicit c =>
		SQL("select * from reminder_person order by reminder_id").as(reminderPerson *)
	}
			  
	def delete(reminderId: Long, personId: Long) {
		DB.withConnection { implicit c =>
			SQL("delete from reminder_person where reminder_id = {reminder_id} and person_id = {person_id}").on(
				'reminder_id -> reminderId,
				'person_id -> personId
			).executeUpdate()
		}
	}
}

