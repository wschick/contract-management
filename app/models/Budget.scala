package models

import com.mysql.jdbc.exceptions.jdbc4._
import java.sql.SQLException

import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession

case class Budget(id: Long,
                 name: String)

object Budget extends Table[Budget]("budget") with DbUtils{
  def id = column[Long]("id") //("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def * = id ~ name <> (Budget.apply _, Budget.unapply _)

  def all(): List[Budget] = withSession {
    Query(Budget).sortBy(_.name) list
  }

  def findById(id: Long): Option[Budget] =withSession {
    (for (a <- Budget if a.id === id) yield a).list.headOption
  }

  def findByName(Name: String): Option[Budget] = withSession {
    //(for (l <- Location if l.code===code) yield l).list.headOption
      (for (b <- Budget if b.name===name) yield b).list.headOption
//    Query(Budget).where(_.name===name).list.headOption
  }

  def nameById(id: Long): Option[String] = withSession {
    findById(id).map(budget => Some(budget.name)).getOrElse(None)
  }

  def create(newBudget: String) = withSession{
    (Budget.name).insert(newBudget)
  }

  def update(id: Long, budget: String) = withSession {
    val q = for { b <- Budget if b.id === id } yield b.name
    q.update(budget)
  }

  def delete(id: Long): Option[String] = withSession{
    try {
      val q = for { b <- Budget if b.id === id } yield b
      q.delete
      return None
    } catch {
      // Sorry this is mysql specific, but I don't think there is a general way to
      // catch a constraint violation exception.
      case e: MySQLIntegrityConstraintViolationException =>
        Some("Can't delete this because something else depends upon it.")
      case e: SQLException =>
        println(e)
        Some("Couldn't delete this budget: " + e.getMessage)
    }
  }

  def options: Seq[(String, String)] = withSession {
    Query(Budget).sortBy(_.name).list.map(c=>(c.id.toString, c.name))
  }
}