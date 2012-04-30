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
			"lastestStartDate" -> optional(date),
			"contractTypes_" -> list(longNumber)
			)
		(
			(showOk, showNearWarning, showFarWarning, showTooLate, showActive, showCancelled, 
				earliestStartDate, latestStartDate, contractTypes_) =>
			{println(contractTypes_); ContractFilter(showOk, showNearWarning, showFarWarning, showTooLate, showActive, showCancelled,
				earliestStartDate.map(d => Some(new LocalDate(d))).getOrElse(None), 
				latestStartDate.map(d => Some(new LocalDate(d))).getOrElse(None),
				contractTypes_)}
		)
		(
			(filter: ContractFilter) => Some((
			filter.showOk, filter.showNearWarning, filter.showFarWarning,
			filter.showTooLate, filter.showActive, filter.showCancelled,
			filter.earliestStartDate.map(d => Some(d.toDate())).getOrElse(None), 
			filter.latestStartDate.map(d => Some(d.toDate())).getOrElse(None), filter.contractTypeIds))
		)
	)


	val contractForm: Form[Contract] = Form(
		mapping(
			"id" -> ignored(NotAssigned:Pk[Long]),
			"vendorContractId" -> nonEmptyText,
			"billingAccount" -> optional(text),
			"name" -> nonEmptyText,
			"description" -> optional(text),
			"cost" -> mapping("mrc" -> nonEmptyText, "nrc" -> nonEmptyText, "currencyId" -> longNumber)
				((mrc, nrc, currencyId) => ContractCosts.create(mrc, nrc, currencyId))
				/*((mrc, nrc, currencyId) => ContractCosts(mrc.toDouble, nrc.toDouble, currencyId))*/
				((cc: ContractCosts) => Some((cc.mrc.toString, cc.nrc.toString, cc.currencyId))),
			/*"mrc" -> nonEmptyText,
			"nrc" -> nonEmptyText,
			"currency" -> longNumber,
			*/
			"aEnd" -> longNumber,
			"zEnd" -> longNumber,
			"startDate" -> date,
			"term" -> mapping("termLength" -> number, "termUnits" -> number)
				((termLength, termUnits) => Term(termLength, TimePeriodUnits.create(termUnits)))
				((t: Term) => Some((t.length, t.units.value))),
			"cancellation" -> mapping("len" -> number, "units" -> number)
				((len, units) => Term(len, TimePeriodUnits.create(units)))
				((t: Term) => Some((t.length, t.units.value))),
			"cancelledDate" -> optional(date),
			"companyId" -> longNumber,
			"contractTypeId" -> longNumber,
			"attention" -> optional(text),
			"budgetId" -> longNumber,
			"isMSA" -> boolean,
			"MSAId" -> optional(longNumber)
		)
		(
			(id, vendorContractId, billingAccount, name, description, cost, aEnd, zEnd,
			startDate, term, cancellation,
			cancelledDate, companyId, contractTypeId, attention, budgetId, isMSA, MSAId) => 
				Contract(NotAssigned, vendorContractId, billingAccount, name, description, cost.mrc,
				cost.nrc, cost.currencyId, Location.findById(aEnd).get, Location.findById(zEnd).get, 
				new LocalDate(startDate), term, cancellation,
				cancelledDate match {
					case Some(date) => Some(new LocalDate(date))
					case None => None
				},
				None, None, /*lastModifyingUser, lastModifiedTime,*/ companyId, ContractType.findById(contractTypeId).get, 
				attention, Budget.findById(budgetId).get, isMSA, MSAId)
				//TODO handle error condiditions better. The findByIds could blow up
		)
		(
			(contract: Contract) => Some((
				contract.id, 
				contract.vendorContractId, 
				contract.billingAccount,
				contract.name, 
				contract.description, 
				ContractCosts(contract.mrc, contract.nrc, contract.currencyId),
				/*contract.mrc.toString, 
				contract.nrc.toString, 
				contract.currencyId,*/
				contract.aEnd.id, 
				contract.zEnd.id,
				contract.startDate.toDate,
				contract.term,
				contract.cancellationPeriod,
				contract.cancelledDate match {
					case Some(date) => Option(date.toDate)
					case None => None
				},
				//contract.lastModifyingUser,
				//contract.lastModifiedTime,
				contract.companyId,
				contract.contractType.id.get,
				contract.attention,
				contract.budget.id.get,
				contract.isMSA,
				contract.MSAId))
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
    //Ok(views.html.contract.list(Contract.all(), filterForm))
		println("<><><><> The request <><><><><>")
		println(request.body)
		println("<><><><> End request <><><><><>")
		filterForm.bind(RequestProcessing.translateToPlayInput(request.body.asFormUrlEncoded.get)).fold(
		//filterForm.bindFromRequest.fold(
			formWithErrors => BadRequest(html.contract.list(Contract.all(), formWithErrors)),
			filter => {
				println("In Contracts, the filter is " + filter)
    		Ok(views.html.contract.list(Contract.filtered(filter), filterForm.fill(filter)))
			}
		)
	}

	// After creating a contract, go to the view page for what you just created.
	def create = Action { implicit request =>
		contractForm.bindFromRequest.fold(
			formWithErrors => { println(formWithErrors); BadRequest(html.contract.new_form(formWithErrors))},
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
							// TODO handle missing company better
							Attachments.changeVendorContractId(Company.findById(existingContract.companyId).get.name, existingContract.vendorContractId, contract.vendorContractId)
							//TODO should redirect to view page or list, depending on where you came from
							Redirect(routes.Contracts.all)
							//Ok(views.html.contract.list(Contract.all(), filterForm))
						} catch {
							case e =>
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
		println("Delete " + id)
		Contract.findById(id).map { existingContract =>
			{
				val result = Attachments.deleteAll(existingContract.companyId, existingContract.vendorContractId)
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
