package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.Location

object Locations extends Controller {
  
	def locationForm = Form(
		tuple (
			"code" -> nonEmptyText,
			"description" -> nonEmptyText
		)
	)

	/** Show everything */
	def all = Action {
    Ok(views.html.location.list(Location.all(), locationForm))
	}

	/** Create a location with the form values. */
	def create = Action { implicit request =>
		locationForm.bindFromRequest.fold(
			formWithErrors => BadRequest(views.html.location.list(Location.all(), formWithErrors)),
			locationTuple => {
				val (code, description) = locationTuple
				Location.create(code, description)
				Redirect(routes.Locations.all)
			}
		)
	}

	/** Put up a form so the user can update the values */
  def edit(id: Long) = Action {
		Location.findById(id).map { existingLocation =>
			Ok(views.html.location.edit(existingLocation, locationForm.fill(existingLocation.code, existingLocation.description)))
		}.getOrElse(NotFound)
	}

	/** Update the database with the form values. */
	def update(id: Long) = Action { implicit request =>
		locationForm.bindFromRequest.fold(
			formWithErrors => {
				Location.findById(id).map { 
					existingLocation => {
						BadRequest(views.html.location.edit( existingLocation, formWithErrors))
					}
				}.getOrElse(NotFound)
			},
			locationTuple => {
				val (code, description) = locationTuple
				Location.update(id, code, description)
				Ok(views.html.location.list(Location.all(), locationForm))
			}
		)
	}

	/** Delete the location. */
	def delete(id: Long) = Action {
		Location.delete(id)
		Redirect(routes.Locations.all)
	}

}
