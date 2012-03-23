package models

case class Term(length: Int, units: TimePeriodUnits) {

	override def toString: String = {
		length + " " + units.asString(length)
	}
}

object Term {
	  
	def create(length: Int, units: TimePeriodUnits) {
		new Term(length, units)
	}

}
