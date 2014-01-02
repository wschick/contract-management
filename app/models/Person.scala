package models

import com.mysql.jdbc.exceptions.jdbc4._
import java.sql.SQLException

import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession

case class Person(
	id: Long, 
	name: String, 
	email: String, 
	telephone: Option[String], 
	companyId: Long)

object Person extends Table[Person]("person") with DbUtils {
  def id = column[Long]("id")
  def name = column[String]("name")
  def email = column[String]("email")
  def telephone = column[String]("telephone")
  def companyId = column[Long]("company_id")
  def * = id ~ name ~ email ~ telephone.? ~ companyId <> (Person.apply _, Person.unapply _)

	def findByName(name: String): Option[Person] = withSession{
    (for(p <- Person if p.name === name) yield p).firstOption
	}

	def findById(id: Long): Option[Person] = withSession{
    (for(p<- Person if p.id===id) yield p).firstOption
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


	def all(): List[Person] = withSession {
    Query(Person).sortBy(_.name).list
	}

  def create(name: String, email: String, telephone: Option[String], companyId: Long): Long = withSession{
    (Person.name ~ Person.email ~ Person.telephone.? ~ Person.companyId).insert(name, email, telephone, companyId)
    val lastInsertId = SimpleFunction.nullary[Long]("LAST_INSERT_ID")
    return (for (p <- Person if p.id === lastInsertId) yield p).list.head.id
  }

  def update(id: Long, name: String, email: String, telephone: Option[String], companyId: Long) = withSession {
    val q = for (p <- Person if p.id===id) yield (p.name ~ p.email ~ p.telephone.? ~ p.companyId)
    q.update(name, email, telephone, companyId)
  }

  def delete(id: Long): Option[String] = withSession{
    try {
      val q = for(p <- Person if p.id===id) yield p
      q.delete
      return None
    } catch {
      // Sorry this is mysql specific, but I don't think there is a general way to
      // catch a constraint violation exception.
      case e: MySQLIntegrityConstraintViolationException =>
        Some("Can't delete this because something else depends upon it.")
      case e: SQLException =>
        println(e)
        Some("Couldn't delete this person: " + e.getMessage)
    }
  }

  def options: Seq[(String, String)] = withSession {
    Query(Person).sortBy(_.name).list.map(p => p.id.toString -> (p.name + " [" + Company.nameById(p.companyId) + "]"))
  }
}

