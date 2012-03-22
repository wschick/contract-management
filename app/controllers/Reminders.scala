package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.Reminder

object Reminders extends Controller {
  
	def reminders = Action {
    Ok(views.html.reminders(Reminder.all(), Reminder.reminderForm))
	}

	def newReminder = Action { implicit request =>
		Reminder.reminderForm.bindFromRequest.fold(
			formWithErrors => BadRequest(views.html.reminders(Reminder.all(), formWithErrors)),
			reminderTuple => {
				val (reminderDate, contractId) = reminderTuple
				Reminder.create(reminderDate, contractId)
				Redirect(routes.Reminders.reminders)
			}
		)
	}

	def deleteReminder(id: Long) = Action {
		Reminder.delete(id)
		Redirect(routes.Reminders.reminders)
	}

}
