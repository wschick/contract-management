package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.Email
import models.Reminder

object Emails extends Controller { 
  
	/** Show everything */
	def test = Action { implicit request =>
		//Email.send("wrk,root", "foo@bar.baz", "test subj", "some alert")
		val reminder = Reminder.findById(1).get
		val result = Email.sendReminder(reminder, List("wrk"), routes.Contracts.view(reminder.contract.id.get).absoluteURL(false))
		Ok(result.getOrElse("Sent ok"))
	}

}
