package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current


case class Budget(
	id: Pk[Long] = NotAssigned,
	name: String
) 


object Budget {

	val budget = {
		get[Pk[Long]]("id") ~ 
		get[String]("name") map {
			case id~name => Budget(id, name)
		}
	}	

	/** Get a list of all contracts. */
	def all(): List[Budget] = DB.withConnection { implicit connection =>
		SQL("select * from budget order by name").as(budget *)
	}


	/** Get a specific budget.

		@param id the id of the budget
		@return the budget, if it exists.

		*/
	def findById(id: Long): Option[Budget] = {
		DB.withConnection { implicit connection =>
			SQL("select * from budget where id = {id}").on('id -> id).as(Budget.budget.singleOpt)
		}
	}

	/** Get the name of a specific budget.

		@param id the id of the budget
		@return the budget name, if it exists.

		*/
	def nameById(id: Long): Option[String] = {
		findById(id).map(budget => Some(budget.name)).getOrElse(None)
	}
			  
	/** Create a budget in the database.

		@param budget A Budget object to be persisted. 
			The unique database key will be provided automatically.
		*/
	def create(newBudget: Budget) {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					insert into budget (name) values ({name})
				"""
				).on(
				'name -> newBudget.name
			).executeUpdate()
		}
	}

	/** Update a contract type in the database.

		@param budget A Budget object to be updated with the values provided 
		*/
	def update(id: Long, budget: Budget) = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					update budget set name={name} where id={id}
				"""
				).on(
				'id -> id,
				'name -> budget.name
			).executeUpdate()
		}
	}
					  
	/** Delete a budget

		@param id the id of the Budget

		*/
	def delete(id: Long) {
		DB.withConnection { implicit connection =>
			SQL("delete from budget where id = {id}").on(
				'id -> id).executeUpdate()
		}
	}
							  
	/** Make Map[String, String] needed for budget select options in a form. 
			This uses the name of the Budget as the visible text.
		*/
	def options: Seq[(String, String)] = DB.withConnection { 
		implicit connection => 
		SQL("select * from budget order by name")
			.as(Budget.budget *).map(c => c.id.toString -> (c.name))
	}

}

