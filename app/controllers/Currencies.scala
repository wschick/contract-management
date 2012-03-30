package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.Currency

object Currencies extends Controller {

	def currencyForm = Form(
		"abbreviation" -> nonEmptyText
	)
  
	def all = Action {
    Ok(views.html.currency.list(Currency.all(), currencyForm))
	}

	def create = Action { implicit request =>
		currencyForm.bindFromRequest.fold(
			errors => BadRequest(views.html.currency.list(Currency.all(), errors)),
			abbreviation => {
				Currency.create(abbreviation)
				Redirect(routes.Currencies.all)
			}
		)
	}

	/** Put up a form so the user can update the values */
  def edit(id: Long) = Action {
		Currency.findById(id).map { existingCurrency =>
			Ok(views.html.currency.edit(existingCurrency, currencyForm.fill(existingCurrency.abbreviation)))
		}.getOrElse(NotFound)
	}

	/** Update the database with the form values. */
	def update(id: Long) = Action { implicit request =>
		currencyForm.bindFromRequest.fold(
			formWithErrors => {
				Currency.findById(id).map { 
					existingCurrency => {
						BadRequest(views.html.currency.edit( existingCurrency, formWithErrors))
					}
				}.getOrElse(NotFound)
			},
			currency => {
				Currency.update(id, currency)
				Ok(views.html.currency.list(Currency.all(), currencyForm))
			}
		)
	}

	def delete(id: Long) = Action {
		Currency.delete(id)
		Redirect(routes.Currencies.all)
	}

}
