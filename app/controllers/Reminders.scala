package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.joda.time._
import anorm._

import models.Reminder

case class ReminderAndPeople(reminder: Reminder, people: List[Long]);

object Reminders extends Controller {
  
	/*


1) Use the attached multi select input helper
2) You'll need to remap the incoming form data before binding it to the underlying Form object. You could use this Java code as a guideline to bind to the Form (instead of using the standard Form<T>.bindFromRequest):

	Map<String, String> newData = new HashMap<String, String>();
	Map<String, String[]> urlFormEncoded = play.mvc.Controller.request().body().asFormUrlEncoded();
	if (urlFormEncoded != null) {
		for (String key : urlFormEncoded.keySet()) {
			String[] value = urlFormEncoded.get(key);
			if (value.length == 1) {
				newData.put(key, value[0]);
			} else if (value.length > 1) {
				for (int i = 0; i < value.length; i++) {
					newData.put(key + "[" + i + "]", value[i]);
				}
			}
		}
	}
	// bind to the MyEntity form object
	Form<MyEntity> saveForm = new Form<MyEntity>(MyEntity.class).bind(newData);
*/
/*
	def reminderAndPeopleForm = Form(
		mapping (
			"id" -> ignored(NotAssigned:Pk[Long]),
			"reminder_date" -> date,
			"contract_id" -> longNumber,
			"sent" -> boolean,
			"people" -> List[longNumber]
		)
		(
			(id, reminder_date, contract_id, sent, people) =>
				ReminderAndPeople(Reminder(id, new LocalDate(reminder_date), contract_id, sent), List(people))
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
	*/


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
		println(request.body)
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
