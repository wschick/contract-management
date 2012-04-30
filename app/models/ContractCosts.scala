package models

case class ContractCosts(
	mrc: Double,
	nrc: Double,
	currencyId: Long) {

	def currencyAbbreviation(): String = {
		val currency = Currency.findById(currencyId)
		if (currency == None) return "???"
		else return currency.get.abbreviation
	}
}

object ContractCosts {

	def create(mrc: String, nrc: String, currencyId: Long): ContractCosts = {
		ContractCosts(mrc.toDouble, nrc.toDouble, currencyId)
	}
}
