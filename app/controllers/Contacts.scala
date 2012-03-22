package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.Contact

object Contacts extends Controller {
  
	def contacts = Action {
    Ok(views.html.contacts(Contact.all(), Contact.contactForm))
	}

	def newContact = Action { implicit request =>
		Contact.contactForm.bindFromRequest.fold(
			formWithErrors => BadRequest(views.html.contacts(Contact.all(), formWithErrors)),
			contactTuple => {
				val (name, email, telephone, companyId) = contactTuple
				Contact.create(name, email, telephone, companyId)
				Redirect(routes.Contacts.contacts)
			}
		)
	}

	def deleteContact(id: Long) = Action {
		Contact.delete(id)
		Redirect(routes.Contacts.contacts)
	}

}
