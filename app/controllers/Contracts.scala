package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.joda.time._
import java.io.File
import java.io.IOException
import java.sql.SQLException

import views._
import anorm._

import models.Attachment
import models.Budget
import models.Company
import models.Contract
import models.ContractCosts
import models.ContractFilter
import models.ContractType
import models.DateUtil
import models.Location
import models.OptionList
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
			"lastestStartDate" -> optional(date),
			"contractTypes_" -> optional(list(longNumber)),
			"vendorIds" -> optional(list(longNumber)),
			"budgetIds" -> optional(list(longNumber)),
			"locationIds" -> optional(list(longNumber)),
			"showMSA" -> optional(longNumber),
			"vendorContractIdMatches" -> optional(text),
			"extraInfoMatches" -> optional(text),
			"maximumDaysToCancel" -> optional(number)
			)
		(
			(showOk, showNearWarning, showFarWarning, showTooLate, showActive, showCancelled, 
				earliestStartDate, latestStartDate, contractTypes_, vendorIds, budgetIds, locationIds,
				showMSA, vendorContractIdMatches, extraInfoMatches, maximumDaysToCancel) =>
			{ContractFilter(showOk, showNearWarning, showFarWarning, showTooLate, showActive, showCancelled,
				earliestStartDate.map(d => Some(new LocalDate(d))).getOrElse(None), 
				latestStartDate.map(d => Some(new LocalDate(d))).getOrElse(None),
				new OptionList(contractTypes_, "contract_type_id"), 
				new OptionList(vendorIds, "vendor_id"),
				new OptionList(budgetIds, "budget_id"),
				new OptionList(locationIds, "a_end_id"),
				showMSA, vendorContractIdMatches, extraInfoMatches, maximumDaysToCancel)}
		)
		(
			(filter: ContractFilter) => Some((
			filter.showOk, filter.showNearWarning, filter.showFarWarning,
			filter.showTooLate, filter.showActive, filter.showCancelled,
			filter.earliestStartDate.map(d => Some(d.toDate())).getOrElse(None), 
			filter.latestStartDate.map(d => Some(d.toDate())).getOrElse(None), 
			filter.contractTypeIds.list,
			filter.vendorIds.list, 
			filter.budgetIds.list, 
			filter.locationIds.list,
			filter.showMSA, filter.vendorContractIdMatches, filter.extraInfoMatches, filter.maximumDaysToCancel
			))
		)
	)


	val contractForm: Form[Contract] = Form(
		mapping(
      "id" -> optional(longNumber),
			"vendorId" -> longNumber,
			"vendorContractId" -> nonEmptyText,
			"billingAccount" -> optional(text),
			"isMSA" -> boolean,
			"MSAId" -> optional(longNumber),
			"extraInfo" -> optional(text),
			"description" -> optional(text),
			"contractTypeId" -> longNumber,
			"aEnd" -> longNumber,
			"zEnd" -> longNumber,
			"cost" -> mapping("mrc" -> nonEmptyText, "nrc" -> nonEmptyText, 
				"currencyId" -> longNumber, "budgetId" -> longNumber)
				((mrc, nrc, currencyId, budgetId) => ContractCosts.create(mrc, nrc, currencyId, budgetId))
				((cc: ContractCosts) => Some((cc.mrc.toString, cc.nrc.toString, cc.currency.id, cc.budget.id))),
			"startDate" -> date(DateUtil.dateFmtString),
			"term" -> mapping("termLength" -> number, "termUnits" -> number)
				((termLength, termUnits) => Term(termLength, TimePeriodUnits.create(termUnits)))
				((t: Term) => Some((t.length, t.units.value))),
			"cancellation" -> mapping("len" -> number, "units" -> number)
				((len, units) => Term(len, TimePeriodUnits.create(units)))
				((t: Term) => Some((t.length, t.units.value))),
			"cancelledDate" -> optional(date(DateUtil.dateFmtString)),
			"autoRenewPeriod" -> mapping("len" -> optional(number), "units" -> optional(number))
				((len, units) => {
					if (len == None || units == None) None 
					else Some(Term(len.get, TimePeriodUnits.create(units.get)))
					})
				((t: Option[Term]) => {
					if (t == None) None
					else Some((Some(t.get.length), Some(t.get.units.value)))
					}),
			"attention" -> optional(text)
		)
		(
			(id, vendorId, vendorContractId, billingAccount, isMSA, MSAId,
			extraInfo, description, contractTypeId, 
			aEnd, zEnd,
			cost, 
			startDate, term, cancellation, cancelledDate, autoRenewPeriod,
			attention 
			) => 
				Contract(None, Company.findById(vendorId).get, vendorContractId, billingAccount, isMSA, MSAId,
				extraInfo, description, ContractType.findById(contractTypeId).get, 
				Location.findById(aEnd).get, Location.findById(zEnd).get, 
				cost,
				/*cost.mrc, cost.nrc, cost.currencyId, Budget.findById(budgetId).get,*/
				new LocalDate(startDate), term, cancellation,
				cancelledDate match {
					case Some(date) => Some(new LocalDate(date))
					case None => None
				},
				autoRenewPeriod,
				attention, 
				None, None /*lastModifyingUser, lastModifiedTime,*/ )
				//TODO handle lastmodifying user and lastmodified time better
				//TODO handle error condiditions better. The findByIds could blow up
		)
		(
			(contract: Contract) => Some((
				contract.id,
				contract.vendor.id,
				contract.vendorContractId, 
				contract.billingAccount,
				contract.isMSA,
				contract.MSAId,
				contract.extraInfo, 
				contract.description, 
				contract.contractType.id,
				contract.aEnd.id, 
				contract.zEnd.id,
				contract.cost,
				/*ContractCosts(contract.mrc, contract.nrc, contract.currencyId, contract.budget.id.get),*/
				contract.startDate.toDate,
				contract.term,
				contract.cancellationPeriod,
				contract.cancelledDate match {
					case Some(date) => Option(date.toDate)
					case None => None
				},
				contract.autoRenewPeriod,
				contract.attention
				//contract.lastModifyingUser,
				//contract.lastModifiedTime
				//TODO handle lastmodifying user and lastmodified time better
				))
		)
	)

  def emptyForm = Action {
    Ok(html.contract.new_form(contractForm))
  }

	def all = Action {
		val filter = new ContractFilter
    Ok(views.html.contract.list(Contract.filtered(filter), filterForm.fill(filter)))
		//Ok("deleteme")
	}

	def filtered = Action { implicit request =>
    //Ok(views.html.contract.list(Contract.all(), filterForm))
		Logger.debug("<><><><> The request <><><><><>")
		Logger.debug(request.body.toString)
		Logger.debug("<><><><> End request <><><><><>")
		filterForm.bind(RequestProcessing.translateToPlayInput(request.body.asFormUrlEncoded.get)).fold(
		//filterForm.bindFromRequest.fold(
			formWithErrors => BadRequest(html.contract.list(Contract.all(), formWithErrors)),
			filter => {
				Logger.debug("In Contracts, the filter is " + filter)
    		Ok(views.html.contract.list(Contract.filtered(filter), filterForm.fill(filter)))
			}
		)
	}

	// After creating a contract, go to the view page for what you just created.
	def create = Action { implicit request =>
		contractForm.bindFromRequest.fold(
			formWithErrors => { Logger.debug(formWithErrors.toString); BadRequest(html.contract.new_form(formWithErrors))},
			contract => {
				val newId = Contract.create(contract)
				Contract.findById(newId).map { existingContract =>
					Redirect(routes.Contracts.view(newId))
				}.getOrElse(NotFound)
			}
		)
	}

  def edit(id: Long) = Action {
		Contract.findById(id).map { existingContract =>
			Ok(html.contract.edit_form(existingContract, contractForm.fill(existingContract)))
		}.getOrElse(NotFound)
	}

  def update(id: Long) = Action { implicit request =>
		contractForm.bindFromRequest.fold(
			formWithErrors => {
				Contract.findById(id).map { 
					existingContract => {
						BadRequest(views.html.contract.edit_form(existingContract, formWithErrors))
					}
				}.getOrElse(NotFound)
			},
			contract => {
				// See if there will be a duplicate contract id and complain if so
				// TODO
				Contract.findById(id).map { 
					existingContract => {
						try {
							Contract.update(id, contract)
							// If you changed the vendorContractId (used for filing), change the location of attachments.
							// TODO handle missing vendor better
							Attachments.changeVendorContractId(Company.findById(existingContract.vendor.id).get.name, existingContract.vendorContractId, contract.vendorContractId)
							//TODO should redirect to view page or list, depending on where you came from
							Redirect(routes.Contracts.all)
							//Ok(views.html.contract.list(Contract.all(), filterForm))
						} catch {
							case e:Throwable =>
								Ok(html.contract.edit_form(existingContract, contractForm.bindFromRequest, errorMessage = Some("A problem: " + e.getMessage)))
						}
					}
				}.getOrElse(NotFound)
			}
		)
	}


	def view(id: Long) = Action { implicit request =>
		Contract.findById(id).map { existingContract =>
			Ok(html.contract.view(existingContract, "Contract " + existingContract.vendorIdString(), ""))
		}.getOrElse(NotFound)
	}

	def delete(id: Long) = Action {
		Logger.debug("Delete " + id)
		Contract.findById(id).map { existingContract =>
			{
				val result = Attachments.deleteAll(existingContract.vendor.id, existingContract.vendorContractId)
				if (result == None) {
					Contract.delete(id)
					Redirect(routes.Contracts.filtered)
				} else {
					// We are currently only allowing delete from the view page.
					Ok(html.contract.view(existingContract, "Contract " + existingContract.vendorIdString(), "", 
						errorMessage = Some("Couldn't delete attachments: " + result)))
					//Redirect(routes.Contracts.filtered)
				}
			}
		}.getOrElse(NotFound)
	}

}
