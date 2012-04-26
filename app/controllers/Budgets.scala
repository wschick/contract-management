package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import anorm._
import models.Budget

object Budgets extends Controller {
  
	val budgetForm: Form[Budget] = Form(
		mapping(
			"id" -> ignored(NotAssigned:Pk[Long]),
			"name" -> nonEmptyText
		)
		(Budget.apply)(Budget.unapply)
	)


	/** Show all the budgets. */
	def all = Action {
    Ok(views.html.budget.list(Budget.all(), budgetForm))
	}

	/** Handle a request to make a new budget */
	def create = Action { implicit request =>
		budgetForm.bindFromRequest.fold(
			formWithErrors => BadRequest(html.budget.list(Budget.all(), formWithErrors)),
			budget => {
				Budget.create(budget)
				Ok(html.budget.list(Budget.all(), budgetForm))
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
				Ok(html.budget.list(Budget.all(), budgetForm))
			}
		)
	}

	/** Handle a budget delete */
	def delete(id: Long) = Action {
		Budget.delete(id)
		Redirect(routes.Budgets.all)
	}
  
}
