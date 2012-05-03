package models

sealed trait TimePeriodUnits { 
	val value: Int
	def name: String 
	def pluralName: String
	
	def asString(quantity: Int): String = {
		if (quantity == 1) name
		else pluralName
	}
}

object TimePeriodUnits {

	def options: Seq[(String, String)] =
		Seq( (DAY.value.toString, "Day(s)"), (MONTH.value.toString, "Month(s)"), (YEAR.value.toString, "Year(s)") )

	def create(value: Int): TimePeriodUnits = {
		value match {
			case DAY.value => DAY
			case MONTH.value => MONTH
			case YEAR.value => YEAR
			// TODO throw some reasonable exception here, instead of making bogus
			case _ => throw new IllegalArgumentException("Unknown TimePeriodUnits value " + value)
		}
}

case object DAY extends TimePeriodUnits { 
	val value = 0
	val name = "Day"
	val pluralName = "Days"
}

case object MONTH extends TimePeriodUnits { 
	val value = 1
	val name = "Month" 
	val pluralName = "Months"
}

case object YEAR extends TimePeriodUnits { 
	val value = 2
	val name = "Year" 
	val pluralName = "Years"
}

}


//case class TimePeriod(length: Int, units: TimePeriodUnits)

