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

	def all = Action {
    Ok(views.html.company.list(Company.all(), companyForm))
	}

	def create = Action { implicit request =>
		companyForm.bindFromRequest.fold(
			formWithErrors => BadRequest(views.html.company.list(Company.all(), formWithErrors)),
			companyTuple => {
				val (name, primaryContactId) = companyTuple
				Company.create(name, primaryContactId)
				Redirect(routes.Companies.all)
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
				Company.update(id, name, primaryContactId)
				Ok(views.html.company.list(Company.all(), companyForm))
			}
		)
	}

	def delete(id: Long) = Action {
		Company.delete(id)
		Redirect(routes.Companies.all)
	}

}
