package models
import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession

case class ReminderPerson(reminderId: Long, personId: Long) {
}

object ReminderPerson  extends Table[ReminderPerson]("reminder_person") with DbUtils {
  def reminderId = column[Long]("reminder_id")
  def personId = column[Long]("person_id")

  def * = reminderId ~ personId  <> (ReminderPerson.apply _, ReminderPerson.unapply _)

	def findByReminderId(id: Long): List[ReminderPerson] = withSession{
    val q = for( rp <- ReminderPerson if rp.reminderId===id) yield rp
    q.list
	}

	def all(): List[ReminderPerson] = withSession {
    val q = for(rp <- ReminderPerson) yield rp
    q.sortBy(_.reminderId).list
	}
			  
	def delete(reminderId: Long, personId: Long) = withSession{
    val q = for(rp <- ReminderPerson
                if rp.reminderId===reminderId && rp.personId===personId) yield rp
    q.delete
	}
}

