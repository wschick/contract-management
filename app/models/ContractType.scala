package models

//import anorm._
//import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import com.mysql.jdbc.exceptions.jdbc4._
import java.sql.SQLException
import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession

case class ContractType(id: Option[Long], name: String)

object ContractType extends Table[ContractType]("contract_type") with DbUtils{

  def id = column[Long]("id")
  def name = column[String]("name")
  def * = id.? ~ name <> (ContractType.apply _, ContractType.unapply _)

  def all(): List[ContractType] = withSession {
    Query(ContractType).sortBy(_.name) list
  }

  def findById(id: Long): Option[ContractType] =withSession {
    (for (a <- ContractType if a.id === id) yield a).list.headOption
  }

	def nameById(id: Long): Option[String] = {
		findById(id).map(contractType => Some(contractType.name)).getOrElse(None)
	}

  def findByName(name: String): Option[ContractType] = withSession{
    (for (c <- ContractType if c.name === name) yield c).list.headOption
  }

  def create(contractType: ContractType) = withSession{
    (ContractType.name).insert(contractType.name)
  }

  def update(id: Long, contractType: ContractType) = withSession {
    val q = for { c <- ContractType if c.id === id } yield c.name
    q.update(contractType.name)
  }

	def delete(id: Long): Option[String] = withSession{
		try {
      val q = for { c <- ContractType if c.id === id } yield c
      q.delete
			return None
		} catch {
			// Sorry this is mysql specific, but I don't think there is a general way to 
			// catch a constraint violation exception.
			case e: MySQLIntegrityConstraintViolationException => 
				Some("Can't delete this because something else depends upon it.")
			case e: SQLException =>
				println(e)
				Some("Couldn't delete this contract type: " + e.getMessage)
		}
	}

  def options: Seq[(String, String)] = withSession {
    Query(ContractType).sortBy(_.name).list.map(c => c.id.toString -> (c.name))
  }

}

