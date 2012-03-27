package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.Currency
import models.Location
import models.Contract

object Application extends Controller {
  
	// Forms

	def companyForm = Form(
		tuple(
			"name" -> nonEmptyText,
			"contact name" -> nonEmptyText,
			"contact email" -> nonEmptyText,
			"contact phone" -> nonEmptyText
			// TODO: do better validation of email and phone
		)
	)

	// End Forms

  def index = Action {
    Ok(views.html.index())
  }

  def settings = Action {
    Ok(views.html.settings())
  }

	def currencies = Action {
    Ok(views.html.currencies(Currency.all(), Currency.currencyForm))
	}

	def newCurrency = Action { implicit request =>
		Currency.currencyForm.bindFromRequest.fold(
			errors => BadRequest(views.html.currencies(Currency.all(), errors)),
			label => {
				Currency.create(label)
				Redirect(routes.Application.currencies)
			}
		)
	}

	def deleteCurrency(id: Long) = Action {
		Currency.delete(id)
		Redirect(routes.Application.currencies)
	}

}
