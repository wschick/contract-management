package models

import slick.session.Database.threadLocalSession
import play.api.db.DB
import scala.slick.session.Database
import play.api.Play.current

trait DbUtils {
  lazy val database = Database.forDataSource(DB.getDataSource())
  def withSession[T](f: => T): T = database.withSession(f)
}