package models

object TimePeriodUnits {
	def options: Seq[(String, String)] =
		Seq( ("0", "Days"), ("1", "Months"), ("2", "Years") )
}

class TimePeriodUnits extends BindableEnum {

	val Days, Months, Years = Value // create enumerated values

}

case class TimePeriod(length: Int, units: TimePeriodUnits)

