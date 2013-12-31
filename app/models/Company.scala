package models

import com.mysql.jdbc.exceptions.jdbc4._
import java.sql.SQLException

import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession

case class Company(
	id: Long, 
	name: String, 
	primaryContactId: Option[Long])

object Company extends Table[Company]("company") with DbUtils{
  def id = column[Long]("id")
  def name = column[String]("name")
  def primaryContactId = column[Long]("primary_contact_id")

  def * = id ~ name ~ primaryContactId.? <> (Company.apply _, Company.unapply _)

  def findById(id: Long): Option[Company] = withSession{
    (for (c <- Company if c.id === id) yield c).list.headOption
  }

  def findByName(name: String): Option[Company] = withSession{
    (for (c <- Company if c.name === name) yield c).list.headOption
  }

  def nameById(id: Long): String = {
    val c = findById(id);
    c match {
      case Some(companyObj) => companyObj.name
      case None => ""
    }
  }

  def nameById(id: Option[Long]): String = {
    id match {
      case Some(id) => nameById(id)
      case None => ""
    }
  }

  def all(): List[Company] = withSession {
    Query(Company).sortBy(_.name) list
  }

  def create(name: String, primaryContactId: Option[Long]):Long = withSession{
    (Company.name ~ Company.primaryContactId.?).insert(name, primaryContactId)
    val lastInsertId = SimpleFunction.nullary[Long]("LAST_INSERT_ID")
    return (for (c <- Company if c.id === lastInsertId) yield c).list.head.id
  }


  def update(id: Long, name: String, primaryContactId: Option[Long]) = withSession{
    val q = for {c <- Company if c.id === id} yield (c.name ~ c.primaryContactId)
    q.update(name, primaryContactId.get)
  }

  def delete(id: Long): Option[String] = withSession{
    try {
      val q = Query(Company).filter(_.id === id)
      q.delete
      return None
    } catch {
      // Sorry this is mysql specific, but I don't think there is a general way to
      // catch a constraint violation exception.
      case e: MySQLIntegrityConstraintViolationException =>
        Some("Can't delete this because something else depends upon it.")
      case e: SQLException =>
        println(e)
        Some("Couldn't delete this company: " + e.getMessage)
    }
  }

  def options: Seq[(String, String)] = withSession {
    Query(Company).sortBy(_.name).list.map(c=>(c.id.toString, c.name))
  }
}