package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.Person

object Persons extends Controller {

	def personForm = Form(
		tuple (
			"name" -> nonEmptyText,
			"email" -> nonEmptyText,
			"telephone" -> optional(nonEmptyText),
			"company_id" -> longNumber
		)
	)
  
	def all(error: Option[String] = None) = Action {
    Ok(views.html.person.list(Person.all(), personForm, error))
	}

	def create = Action { implicit request =>
		personForm.bindFromRequest.fold(
			formWithErrors => BadRequest(views.html.person.list(Person.all(), formWithErrors, None)),
			person => {
				val (name, email, telephone, companyId) = person
				Person.create(name, email, telephone, companyId)
				Redirect(routes.Persons.all(None))
			}
		)
	}

  def edit(id: Long) = Action {
		Person.findById(id).map { existing =>
			Ok(views.html.person.edit(existing, personForm.fill(existing.name, existing.email, existing.telephone, existing.companyId)))
		}.getOrElse(NotFound)
	}

	def update(id: Long) = Action { implicit request =>
		personForm.bindFromRequest.fold(
			formWithErrors => {
				Person.findById(id).map { 
					existingPerson => {
						BadRequest(views.html.person.edit( existingPerson, formWithErrors))
					}
				}.getOrElse(NotFound)
			},
			person => {
				val (name, email, telephone, companyId) = person
				Person.update(id, name, email, telephone, companyId)
				Ok(views.html.person.list(Person.all(), personForm, None))
			}
		)
	}
					  
	def delete(id: Long) = Action {
		Redirect(routes.Persons.all(Person.delete(id)))
	}

}
