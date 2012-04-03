package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format._
import org.joda.time._
import anorm._

import models.Reminder
import models.ReminderAndPeople


object ChoiceList extends Formatter[List[Long]]
{
	def stringFormat: Formatter[String] = new Formatter[String] {
    def bind(key: String, data: Map[String, String]) = data.get(key).toRight(Seq(FormError(key, "error.required", Nil)))
    def unbind(key: String, value: String) = Map(key -> value)
  }

	// Look at format/Format.scala
	// Given form data, make a List[Long]
	def bind(key: String, data: Map[String, String]) = {
		stringFormat.bind(key, data).right.flatMap { s =>
			scala.util.control.Exception.allCatch[List[Long]]
				.either(List[Long](1,2)) // TODO put in real list
				.left.map(e => Seq(FormError(key, "error.choices", Nil)))
		}
	}

	// Given a List[Long], make what the form needs.
	def unbind(key: String, value: List[Long]) = 
		Map(key -> "1,2")
		// TODO given List[Long], generate comma separated string"1,2")
}

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
			(reminderAndPeople: ReminderAndPeople) => Some ((
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
			"sent" -> boolean,
			"people" -> of[List[Long]](ChoiceList)
		)
		(
			(id, reminder_date, contract_id, sent, people) =>
				ReminderAndPeople(new Reminder(id, new LocalDate(reminder_date), contract_id, sent), people)
		)
		(
			(rp: ReminderAndPeople) => Some ((
				rp.reminder.id,
				rp.reminder.reminderDate.toDate,
				rp.reminder.contractId,
				rp.reminder.sent,
				rp.people
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
				// TODO create the reminder people entries, too
				Reminder.create(reminder.reminder)
				Redirect(routes.Reminders.all)
			}
		)
	}

  def edit(id: Long) = Action {
		Reminder.findById(id).map { reminder =>
			//Ok(views.html.reminder.edit(reminder, reminderForm.fill((reminder.reminderDate.toDate, reminder.contractId))))
			// TODO handle person list.
			Ok(views.html.reminder.edit(reminder, reminderForm.fill(new ReminderAndPeople(reminder, List[Long]() /* this should be people list */))))
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
				//TODO handle people list
				Reminder.update(id, reminder.reminder)
				Ok(views.html.reminder.list(Reminder.all(), reminderForm))
			}
		)
	}

	def delete(id: Long) = Action {
		Reminder.delete(id)
		Redirect(routes.Reminders.all)
	}

}
