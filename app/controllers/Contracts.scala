package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.joda.time._

import views._
import anorm._

import models.Contract
import models.ContractFilter
import models.Location
import models.Term
import models.TimePeriodUnits

object Contracts extends Controller {
  
	// Forms

	// TODO change mrc and nrc to BigDecimal

	val filterForm: Form[ContractFilter] = Form(
		mapping (
			"showOk" -> boolean,
			"showNearWarning" -> boolean,
			"showFarWarning" -> boolean,
			"showTooLate" -> boolean,
			"showActive" -> boolean,
			"showCancelled" -> boolean,
			"earliestStartDate" -> optional(date),
			"lastestStartDate" -> optional(date)
			)
		(
			(showOk, showNearWarning, showFarWarning, showTooLate, showActive, showCancelled, 
				earliestStartDate, latestStartDate) =>
			ContractFilter(showOk, showNearWarning, showFarWarning, showTooLate, showActive, showCancelled,
				earliestStartDate.map(d => Some(new LocalDate(d))).getOrElse(None), 
				latestStartDate.map(d => Some(new LocalDate(d))).getOrElse(None))
		)
		(
			(filter: ContractFilter) => Some((
			filter.showOk, filter.showNearWarning, filter.showFarWarning,
			filter.showTooLate, filter.showActive, filter.showCancelled,
			filter.earliestStartDate.map(d => Some(d.toDate())).getOrElse(None), 
			filter.latestStartDate.map(d => Some(d.toDate())).getOrElse(None)))
		)
	)


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
			"cancelledDate" -> optional(date),
			//"reminderPeriod" -> optional(number),
			//"reminderPeriodUnits" -> optional(number),
			"lastModifyingUser" -> optional(text),
			"lastModifiedTime" -> optional(date),
			"companyId" -> longNumber
		)
		(
			(id, contractId, name, description, mrc, nrc, currency, aEnd, zEnd,
			startDate, term, termUnits, cancellationPeriod, cancellationPeriodUnits,
			cancelledDate, 
			//reminderPeriod, reminderPeriodUnits, 
			lastModifyingUser, lastModifiedTime, companyId) => 
				Contract(NotAssigned, contractId, name, description, mrc.toDouble, 
				nrc.toDouble, currency, Location.findById(aEnd).get, Location.findById(zEnd).get, 
				new LocalDate(startDate), Term(term, TimePeriodUnits.create(termUnits)), 
				Term(cancellationPeriod, TimePeriodUnits.create(cancellationPeriodUnits)), 
				//cancelledDate.map(date => Some(new LocalDate(date)).getOrElse(None)),
				cancelledDate match {
					case Some(date) => Some(new LocalDate(date))
					case None => None
				},
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
				contract.startDate.toDate,
				contract.term.length,
				contract.term.units.value,
				contract.cancellationPeriod.length,
				contract.cancellationPeriod.units.value,
				contract.cancelledDate match {
					case Some(date) => Option(date.toDate)
					case None => None
				},
				//contract.reminderPeriod,
				//contract.reminderPeriodUnits,
				contract.lastModifyingUser,
				contract.lastModifiedTime,
				contract.companyId))
		)
	)

  def emptyForm = Action {
    Ok(html.contract.new_form(contractForm))
  }

	def all = Action {
		val filter = new ContractFilter
    Ok(views.html.contract.list(Contract.filtered(filter), filterForm.fill(filter)))
	}

	def filtered = Action { implicit request =>
    Ok(views.html.contract.list(Contract.all(), filterForm))
		filterForm.bindFromRequest.fold(
			formWithErrors => BadRequest(html.contract.list(Contract.all(), formWithErrors)),
			filter => {
    		Ok(views.html.contract.list(Contract.filtered(filter), filterForm.fill(filter)))
			}
		)
	}

	def create = Action { implicit request =>
		contractForm.bindFromRequest.fold(
			formWithErrors => BadRequest(html.contract.form(formWithErrors)),
			contract => {
				Contract.create(contract)
				Ok(html.contract.view(contract, "Contact created", "You just created a contract:"))
			}
		)
	}

  def edit(id: Long) = Action {
		Contract.findById(id).map { existingContract =>
			Ok(html.contract.edit_form(contractForm.fill(existingContract)))
		}.getOrElse(NotFound)
	}

  def update(id: Long) = Action { implicit request =>
		contractForm.bindFromRequest.fold(
			formWithErrors => {
				Contract.findById(id).map { 
					existingContract => {
						println("Form error")
						BadRequest(views.html.contract.form( formWithErrors))
					}
				}.getOrElse(NotFound)
			},
			contract => {
				Contract.update(id, contract)
				Ok(views.html.contract.list(Contract.all(), filterForm))
			}
		)
	}


	def view(id: Long) = Action { implicit request =>
		Contract.findById(id).map { existingContract =>
			Ok(html.contract.view(existingContract, "Contract " + existingContract.contractId, ""))
		}.getOrElse(NotFound)
	}

	def delete(id: Long) = Action {
		Contract.delete(id)
		Redirect(routes.Contracts.filtered)
	}
  
}
