package models

case class ContractCosts(
	mrc: Double,
	nrc: Double,
	currency: Currency,
	budget: Budget) {

	def currencyAbbreviation(): String = {
		currency.abbreviation
	}
}

object ContractCosts {

	def create(mrc: Double, nrc: Double, currencyId: Long, budgetId: Long): ContractCosts = {
		ContractCosts(mrc, nrc, Currency.findById(currencyId).get, Budget.findById(budgetId).get)
		// TODO handle missing currency and budget better
	}

	def create(mrc: String, nrc: String, currencyId: Long, budgetId: Long): ContractCosts = {
		ContractCosts.create(mrc.toDouble, nrc.toDouble, currencyId, budgetId)
	}
}
