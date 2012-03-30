package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.joda.time._
import anorm._

import models.Reminder

case class ReminderForm(reminder: Reminder, people: List[Long]);

object Reminders extends Controller {
  
	def reminderForm = Form(
		/*tuple (
			"reminder_date" -> date,
			"contract_id" -> longNumber
		)*/
		mapping (
			"id" -> ignored(NotAssigned:Pk[Long]),
			"reminder_date" -> date,
			"contract_id" -> longNumber,
			"sent" -> boolean
		)
		(
			(id, reminder_date, contract_id, sent) =>
				Reminder(id, new LocalDate(reminder_date), contract_id, sent)
		)
		(
			(reminder: Reminder) => Some ((
				reminder.id,
				reminder.reminderDate.toDate,
				reminder.contractId,
				reminder.sent
			))
		)
	)

	def all = Action {
    Ok(views.html.reminder.list(Reminder.all(), reminderForm))
	}

	def create = Action { implicit request =>
		reminderForm.bindFromRequest.fold(
			formWithErrors => BadRequest(views.html.reminder.list(Reminder.all(), formWithErrors)),
			reminder => {
				Reminder.create(reminder)
				Redirect(routes.Reminders.all)
			}
		)
	}

  def edit(id: Long) = Action {
		Reminder.findById(id).map { reminder =>
			//Ok(views.html.reminder.edit(reminder, reminderForm.fill((reminder.reminderDate.toDate, reminder.contractId))))
			Ok(views.html.reminder.edit(reminder, reminderForm.fill(reminder)))
		}.getOrElse(NotFound)
	}

	def update(id: Long) = Action { implicit request =>
		reminderForm.bindFromRequest.fold(
			formWithErrors => {
				Reminder.findById(id).map { 
					existingReminder => {
						BadRequest(views.html.reminder.edit( existingReminder, formWithErrors))
					}
				}.getOrElse(NotFound)
			},
			reminder => {
				Reminder.update(id, reminder)
				Ok(views.html.reminder.list(Reminder.all(), reminderForm))
			}
		)
	}

	def delete(id: Long) = Action {
		Reminder.delete(id)
		Redirect(routes.Reminders.all)
	}

}
