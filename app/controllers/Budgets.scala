package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models.Budget

object Budgets extends Controller {
  
	val budgetForm: Form[Budget] = Form(
		mapping(
      "id" -> ignored(None: Option[Long]),
			"name" -> nonEmptyText
		)
		(Budget.apply)(Budget.unapply)
	)


	/** Show all the budgets. */
	def all(error: Option[String] = None) = Action {
    Ok(views.html.budget.list(Budget.all(), budgetForm, error))
	}

	/** Handle a request to make a new budget */
	def create = Action { implicit request =>
		budgetForm.bindFromRequest.fold(
			formWithErrors => BadRequest(html.budget.list(Budget.all(), formWithErrors, None)),
			budget => {
				Budget.create(budget)
				Ok(html.budget.list(Budget.all(), budgetForm, None))
			}
		)
	}

	/** Put up a form so the user can update the values */
  def edit(id: Long) = Action {
		Budget.findById(id).map { existingBudget =>
			Ok(html.budget.edit(existingBudget, budgetForm.fill(existingBudget)))
		}.getOrElse(NotFound)
	}

	def update(id: Long) = Action { implicit request =>
		budgetForm.bindFromRequest.fold(
			formWithErrors => {
				Budget.findById(id).map { 
					existingBudget =>
						BadRequest(html.budget.edit( existingBudget, formWithErrors))
				}.getOrElse(NotFound)
			},
			budget => {
				Budget.update(id, budget)
				Ok(html.budget.list(Budget.all(), budgetForm, None))
			}
		)
	}

	/** Handle a budget delete */
	def delete(id: Long) = Action {
		Redirect(routes.Budgets.all(Budget.delete(id)))
	}
  
}
