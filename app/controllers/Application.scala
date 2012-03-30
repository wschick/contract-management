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

}
