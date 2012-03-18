package models

import org.joda.time.DateMidnight

case class Reminder(id: Long, contract: Contract, date: DateMidnight, emails: List[String], sent: Boolean)

object Reminder {
	  
	def all(): List[Reminder] = Nil
			  
	def create(id: Long, contract: Contract, date: DateMidnight, emails: List[String], sent: Boolean) {}
					  
	def delete(id: Long) {}
							  
}
