package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.Company

object Companies extends Controller {
  
	def companies = Action {
    Ok(views.html.companies(Company.all(), Company.companyForm))
	}

	def newCompany = Action { implicit request =>
		Company.companyForm.bindFromRequest.fold(
			formWithErrors => BadRequest(views.html.companies(Company.all(), formWithErrors)),
			companyTuple => {
				val (name, primaryContactId) = companyTuple
				Company.create(name, primaryContactId)
				Redirect(routes.Companies.companies)
			}
		)
	}

	def deleteCompany(id: Long) = Action {
		Company.delete(id)
		Redirect(routes.Companies.companies)
	}

}
