package models

import com.mysql.jdbc.exceptions.jdbc4._
import java.sql.SQLException

import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession

case class Currency(id: Long, abbreviation: String)

object Currency extends Table[Currency]("currency") with DbUtils {

  def id = column[Long]("id")
  def abbreviation = column[String]("abbreviation")
  def * = id ~ abbreviation <> (Currency.apply _, Currency.unapply _)

  def all(): List[Currency] = withSession {
    Query(Currency).sortBy(_.abbreviation) list
  }

  def create(abbreviation: String) = withSession{
    (Currency.abbreviation).insert(abbreviation)
  }

  def findById(id: Long): Option[Currency] =withSession {
    (for (a <- Currency if a.id === id) yield a).list.headOption
  }

  def findByAbbreviation(abbrev: String): Option[Currency] = withSession{
    (for (c <- Currency if c.abbreviation === abbrev) yield c).list.headOption
  }

	def stringById(id: Long): String = {
		val c = findById(id);

		c match {
			case Some(currencyObj) => currencyObj.abbreviation
			case None => "None"
		}
	}

  def update(id: Long, abbreviation: String) = withSession {
    val q = for { c <- Currency if c.id === id } yield c.abbreviation
    q.update(abbreviation)
  }

  def delete(id: Long): Option[String] = withSession{
    try {
      val q = for { c <- Currency if c.id === id } yield c
      q.delete
      return None
    } catch {
      // Sorry this is mysql specific, but I don't think there is a general way to
      // catch a constraint violation exception.
      case e: MySQLIntegrityConstraintViolationException =>
        Some("Can't delete this because something else depends upon it.")
      case e: SQLException =>
        println(e)
        Some("Couldn't delete this Currency: " + e.getMessage)
    }
  }

  def options: Seq[(String, String)] = withSession {
    Query(Currency).sortBy(_.abbreviation).list.map(c => c.id.toString -> (c.abbreviation))
  }
}
