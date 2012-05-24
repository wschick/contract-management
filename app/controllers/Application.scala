package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.Currency
import models.Location
import models.Contract

object Application extends Controller {
  
  def settings = Action {
    Ok(views.html.settings())
  }

}
