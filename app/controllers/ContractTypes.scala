package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models.ContractType

object ContractTypes extends Controller {
  
	val contractTypeForm: Form[ContractType] = Form(
		mapping(
			"id" -> ignored(None: Option[Long]),
			"name" -> nonEmptyText
		)
		(ContractType.apply)(ContractType.unapply)
	)


	/** Show all the contract types. */
	def all(error: Option[String] = None) = Action {
    Ok(views.html.contract_type.list(ContractType.all(), contractTypeForm, error))
	}

  //def form = Action {
  //  Ok(html.contract_type.form(contractTypeForm))
  //}

	/** Handle a request to make a new contract type */
	def create = Action { implicit request =>
		contractTypeForm.bindFromRequest.fold(
			formWithErrors => BadRequest(html.contract_type.list(ContractType.all(), formWithErrors, None)),
			contractType => {
				ContractType.create(contractType)
				Ok(html.contract_type.list(ContractType.all(), contractTypeForm, None))
			}
		)
	}

	/** Put up a form so the user can update the values */
  def edit(id: Long) = Action {
		ContractType.findById(id).map { existingContractType =>
			Ok(html.contract_type.edit(existingContractType, contractTypeForm.fill(existingContractType)))
		}.getOrElse(NotFound)
	}

	def update(id: Long) = Action { implicit request =>
		contractTypeForm.bindFromRequest.fold(
			formWithErrors => {
				ContractType.findById(id).map { 
					existingContractType =>
						BadRequest(html.contract_type.edit( existingContractType, formWithErrors))
				}.getOrElse(NotFound)
			},
			contractType => {
				ContractType.update(id, contractType)
				Ok(html.contract_type.list(ContractType.all(), contractTypeForm, None))
			}
		)
	}

	//def viewContractType(id: Long) = Action { implicit request =>
//		ContractType.findById(id).map { existingcontractType =>
//			Ok(html.contract_type.contract_types_list(ContractType.all(), contractTypeForm, None))
//		}.getOrElse(NotFound)
//	}

	/** Handle a contract type delete */
	def delete(id: Long) = Action {
		Redirect(routes.ContractTypes.all(ContractType.delete(id)))
	}
  
}
