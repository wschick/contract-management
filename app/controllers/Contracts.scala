package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
//import org.joda.time._

import views._
import anorm._

import models.Contract
import models.Location
import models.Term
import models.TimePeriodUnits

object Contracts extends Controller {
  
	// Forms

	// TODO change mrc and nrc to BigDecimal

	val contractForm: Form[Contract] = Form(
		mapping(
			"id" -> ignored(NotAssigned:Pk[Long]),
			"contractId" -> nonEmptyText,
			"name" -> nonEmptyText,
			"description" -> optional(text),
			"mrc" -> nonEmptyText,
			"nrc" -> nonEmptyText,
			"currency" -> longNumber,
			"aEnd" -> longNumber,
			"zEnd" -> longNumber,
			"startDate" -> date,
			"term" -> number,
			"termUnits" -> number,
			"cancellationPeriod" -> number,
			"cancellationPeriodUnits" -> number,
			//"reminderPeriod" -> optional(number),
			//"reminderPeriodUnits" -> optional(number),
			"lastModifyingUser" -> optional(text),
			"lastModifiedTime" -> optional(date),
			"companyId" -> longNumber
		)
		(
			(id, contractId, name, description, mrc, nrc, currency, aEnd, zEnd,
			startDate, term, termUnits, cancellationPeriod, cancellationPeriodUnits,
			//reminderPeriod, reminderPeriodUnits, 
			lastModifyingUser, lastModifiedTime, companyId) => 
				Contract(NotAssigned, contractId, name, description, mrc.toDouble, 
				nrc.toDouble, currency, Location.findById(aEnd).get, Location.findById(zEnd).get, 
				startDate, Term(term, TimePeriodUnits.create(termUnits)), 
				Term(cancellationPeriod, TimePeriodUnits.create(cancellationPeriodUnits)), 
				//reminderPeriod, reminderPeriodUnits, 
				lastModifyingUser, lastModifiedTime, companyId)
		)
		(
			(contract: Contract) => Some((
				contract.id, contract.contractId, 
				contract.name, 
				contract.description, 
				contract.mrc.toString, 
				contract.nrc.toString, 
				contract.currencyId,
				contract.aEnd.id, 
				contract.zEnd.id,
				contract.startDate,
				contract.term.length,
				contract.term.units.value,
				contract.cancellationPeriod.length,
				contract.cancellationPeriod.units.value,
				//contract.reminderPeriod,
				//contract.reminderPeriodUnits,
				contract.lastModifyingUser,
				contract.lastModifiedTime,
				contract.companyId))
		)
	)

  def form = Action {
    Ok(html.contract.form(contractForm))
  }

  def editForm(id: Long) = Action {
		Contract.findById(id).map { existingContract =>
			Ok(html.contract.form(contractForm.fill(existingContract)))
		}.getOrElse(NotFound)
	}


	def contracts = Action {
    Ok(views.html.contracts(Contract.all(), contractForm))
	}

	def newContract = Action { implicit request =>
		contractForm.bindFromRequest.fold(
			formWithErrors => BadRequest(html.contract.form(formWithErrors)),
			contract => {
				Contract.create(contract)
				Ok(html.contract.summary(contract, "Contact created", "You just created a contract:"))
			}
		)
	}

	def viewContract(id: Long) = Action { implicit request =>
		Contract.findById(id).map { existingContract =>
			Ok(html.contract.summary(existingContract, "Contract " + existingContract.contractId, ""))
		}.getOrElse(NotFound)
	}

	def deleteContract(id: Long) = Action {
		Contract.delete(id)
		Redirect(routes.Contracts.contracts)
	}
  
}
