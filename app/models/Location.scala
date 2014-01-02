package models

import com.mysql.jdbc.exceptions.jdbc4._
import java.sql.SQLException

import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession

case class Location(id: Long, code: String, description: String, address: Option[String]) {
	def longString: String = {
		code + " (" + description + ")"
	}
}

object Location extends Table[Location]("location") with DbUtils {

  def id = column[Long]("id")
  def code = column[String]("code")
  def description = column[String]("description")
  def address = column[String]("address")
  def * = id ~ code ~ description ~ address.? <> (Location.apply _, Location.unapply _)

  def findById(id: Long): Option[Location] =withSession {
    (for (a <- Location if a.id === id) yield a).list.headOption
  }

	def findByCode(code: String): Option[Location] = withSession {
    (for (l <- Location if l.code===code) yield l).list.headOption
	}

	def codeById(id: Long): String = {
		val c = findById(id);

		c match {
			case Some(locationObj) => locationObj.code
			case None => "None"
		}
	}

  def all(): List[Location] = withSession {
    (for(l <- Location) yield l).sortBy(_.code).list
  }

  def create(code: String, description: String, address: Option[String]) = withSession{
    (Location.code ~ Location.description ~ Location.address.?).insert(code, description, address)
  }

  def update(id: Long, code: String, description: String, address: Option[String]) = withSession {
    val q = for (l <- Location if l.id===id) yield (l.code ~ l.description ~ l.address.?)
    q.update(code, description, address)
  }

	def delete(id: Long): Option[String] = withSession{
		try {
      val q = for(l <- Location if l.id === id) yield l
      q.delete
			return None
		} catch {
			// Sorry this is mysql specific, but I don't think there is a general way to 
			// catch a constraint violation exception.
			case e: MySQLIntegrityConstraintViolationException => 
				Some("Can't delete this because something else depends upon it.")
			case e: SQLException =>
				println(e)
				Some("Couldn't delete this location: " + e.getMessage)
		}
	}

	def asString(id: Long): String = {
		val c = findById(id);

		c match {
			case Some(locationObj) => locationObj.longString
			case None => "None"
		}
	}

  def options: Seq[(String, String)] = withSession{
    Query(Location).sortBy(_.code).list.map(l=> (l.id.toString, l.longString))
  }
}
