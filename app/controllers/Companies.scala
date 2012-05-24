package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.Company

object Companies extends Controller {
  
	def companyForm = Form(
		tuple (
			"name" -> nonEmptyText,
			"primary_contact_id" -> optional(longNumber)
		)
	)

	def all(error: Option[String] = None) = Action {
    Ok(views.html.company.list(Company.all(), companyForm, error))
	}

	/** Popup used for on-the-fly company creation when editing a person. */ 
	def popupCreate = Action { implicit request =>
		companyForm.bindFromRequest.fold(
			formWithErrors => BadRequest(views.html.company.list(Company.all(), formWithErrors)),
			companyTuple => {
				val (name, primaryContactId) = companyTuple
				Company.create(name, primaryContactId)
				Redirect(routes.Persons.all(None))
			}
		)
	}

	def create = Action { implicit request =>
		companyForm.bindFromRequest.fold(
			formWithErrors => BadRequest(views.html.company.list(Company.all(), formWithErrors)),
			companyTuple => {
				val (name, primaryContactId) = companyTuple
				Company.create(name, primaryContactId)
				Redirect(routes.Companies.all(None))
			}
		)
	}

  def edit(id: Long) = Action {
		Company.findById(id).map { company =>
			Ok(views.html.company.edit(company, companyForm.fill(company.name, company.primaryContactId)))
		}.getOrElse(NotFound)
	}

	def update(id: Long) = Action { implicit request =>
		companyForm.bindFromRequest.fold(
			formWithErrors => {
				Company.findById(id).map { 
					existingCompany => {
						BadRequest(views.html.company.edit( existingCompany, formWithErrors))
					}
				}.getOrElse(NotFound)
			},
			company => {
				val (name, primaryContactId) = company
				val existing = Company.findById(id).get

				Company.update(id, name, primaryContactId)

				Attachments.changeCompanyName(existing.name, name)

				Ok(views.html.company.list(Company.all(), companyForm))
			}
		)
	}

	def delete(id: Long) = Action {
		val err  =Company.delete(id)
		Redirect(routes.Companies.all(err))
	}

}
