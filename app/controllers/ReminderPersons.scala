package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.ReminderPerson

object ReminderPersons extends Controller {
  
	/** Show everything */
	def all = Action {
    Ok(views.html.reminder_person.list(ReminderPerson.all()))
	}

	/** Delete the location. */
	def delete(reminderId: Long, personId: Long) = Action {
		ReminderPerson.delete(reminderId, personId)
		Redirect(routes.ReminderPersons.all)
	}

}
