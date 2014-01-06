package models

import java.sql.Date
import org.joda.time._

import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession

case class ReminderAndPeople(reminder: Reminder, people: List[Long])

case class Reminder(
                     id: Option[Long],
                     reminderDate: Date,
                     contractId: Long,
                     sent: Boolean)
{
  def reminderDateStr(): String = DateUtil.format(new LocalDate(reminderDate))
  def contract = Contract.findById(contractId).get
}

object Reminder extends Table[Reminder]("reminder") with DbUtils {

  def id = column[Long]("id")
  def reminderDate = column[Date]("reminder_date")
  def contractId = column[Long]("contract_id")
  def sent = column[Boolean]("sent")

  def * = id.? ~ reminderDate ~ contractId ~ sent <> (Reminder.apply _, Reminder.unapply _)

  def findById(id: Long): Option[Reminder] = withSession {
    (for(r<- Reminder if r.id===id) yield r).firstOption
  }

  def reminderDateById(id: Long): Option[LocalDate] = {
    val c = findById(id);

    c match {
      case Some(reminderObj) => Some(new LocalDate(reminderObj.reminderDate))
      case None => None
    }
  }

  def personNamesForReminder(reminderId: Long, maxPersons: Int = 1): List[String] = withSession {
    val q = for{
      r <- Reminder
      rp <- ReminderPerson
      p <- Person
      if ( r.id===reminderId && r.id === rp.reminderId && rp.personId===p.id)
    } yield p.name
    q.list.take(maxPersons)
  }

  def all(): List[Reminder] = withSession {
    Query(Reminder).sortBy(_.reminderDate).list
  }

  def create(reminder: Reminder) = withSession {
    (Reminder.reminderDate ~ Reminder.contractId).insert(reminder.reminderDate, reminder.contractId)
  }

  //def update(id: Long, reminderDate: LocalDate, contractId: Long) {
  def update(id: Long, reminder: Reminder)  = withSession {
    val q = for(r <- Reminder if r.id===id) yield (r.reminderDate ~ r.contractId ~ r.sent)
    q.update(reminder.reminderDate, reminder.contractId, reminder.sent)
  }

  def delete(id: Long) = withSession {
    val q = for(r <- Reminder if r.id ===id) yield r
    q.delete
  }

  val dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd")

  def formatDate(d: Date): String = {
    dateFormatter.format(d)
  }

  def options: Seq[(String, String)] = withSession {
    Query(Reminder).sortBy(_.reminderDate).list.map(r=>(r.id.toString-> r.reminderDate.toString()))
  }
}
