package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import anorm._
import models.ContractType

object ContractTypes extends Controller {
  
	val contractTypeForm: Form[ContractType] = Form(
		mapping(
			"id" -> ignored(NotAssigned:Pk[Long]),
			"name" -> nonEmptyText
		)
		(ContractType.apply)(ContractType.unapply)
	)


	/** Show all the contract types. */
	def all = Action {
    Ok(views.html.ContractType.list(ContractType.all(), contractTypeForm))
	}

  //def form = Action {
  //  Ok(html.ContractType.form(contractTypeForm))
  //}

	/** Handle a request to make a new contract type */
	def create = Action { implicit request =>
		contractTypeForm.bindFromRequest.fold(
			formWithErrors => BadRequest(html.ContractType.list(ContractType.all(), formWithErrors)),
			contractType => {
				ContractType.create(contractType)
				Ok(html.ContractType.list(ContractType.all(), contractTypeForm))
			}
		)
	}

	/** Put up a form so the user can update the values */
  def edit(id: Long) = Action {
		ContractType.findById(id).map { existingContractType =>
			Ok(html.ContractType.edit(existingContractType, contractTypeForm.fill(existingContractType)))
		}.getOrElse(NotFound)
	}

	def update(id: Long) = Action { implicit request =>
		contractTypeForm.bindFromRequest.fold(
			formWithErrors => {
				ContractType.findById(id).map { 
					existingContractType =>
						BadRequest(html.ContractType.edit( existingContractType, formWithErrors))
				}.getOrElse(NotFound)
			},
			contractType => {
				ContractType.update(id, contractType)
				Ok(html.ContractType.list(ContractType.all(), contractTypeForm))
			}
		)
	}

	//def viewContractType(id: Long) = Action { implicit request =>
//		ContractType.findById(id).map { existingcontractType =>
//			Ok(html.ContractType.contract_types_list(ContractType.all(), contractTypeForm))
//		}.getOrElse(NotFound)
//	}

	/** Handle a contract type delete */
	def delete(id: Long) = Action {
		ContractType.delete(id)
		Redirect(routes.ContractTypes.all)
	}
  
}
