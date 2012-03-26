package models

import org.joda.time._
import java.util.Date
import TimePeriodUnits._

// http://joda-time.sourceforge.net/key_period.html

case class Term(length: Int, units: TimePeriodUnits) {

	val period =  units match {
		case DAY => Period.days(length)
		case MONTH => Period.months(length)
		case YEAR => Period.years(length)
	}

	override def toString: String = {
		length + " " + units.asString(length)
	}

	/** Given a date and this term, return a date that represet the date plus the period */
	def datePlusTerm(startingDate: LocalDate): LocalDate = {
		startingDate.plus(period)
	}

	/** Given a starting date and this term, figure out how long from today until the term ends. */
	def periodUntilTermEnd(startingDate: LocalDate): (Int, Int, Int) = {
		val endOfTerm = startingDate.plus(period)
		val today = new LocalDate()
		val years = Years.yearsBetween(today, endOfTerm)
		val todayPlusYears = today.plus(years)
		val months = Months.monthsBetween(todayPlusYears, endOfTerm)
		val days = Days.daysBetween(todayPlusYears.plus(months), endOfTerm)
		(years.getYears, months.getMonths, days.getDays)
	}

	def periodUntilTermEnd(startingDate: Date): (Int, Int, Int) = {
		periodUntilTermEnd(new LocalDate(new DateTime(startingDate)))
	}

	/** Print a number and a plural or non-plural string. Add "s" to string if the magnitude of units is more than 1.

		@passed units How many of these. It only looks for 0, 1, -1 or anything else
		@passed str The string to pluralize by adding "s" if there are multiples
		@passed printZero If false, print nothing if units is zero.
		@return If units is 0, return an empty string. If units is 1 or -1, return unit. Else append s to string 

		*/
	def pluralize(units: Int, str: String, printZero: Boolean): String = {
		if (units == 0 && !printZero) ""
		else if (units == 1 | units == -1) units + " " + str
		else units + " " + str + "s"
	}

	def periodUntilTermEndString(startingDate: LocalDate): String = {
		val (years, months, days) = periodUntilTermEnd(startingDate)
		var result = pluralize(years, "year", false)
		if (result != "") result = result + ", "
		result = result + pluralize(months, "month", result != "")
		if (result != "") result = result + ", "
		result = result + pluralize(days, "day", result != "")
		return result
	}
}

object Term {
	  
	def create(length: Int, units: TimePeriodUnits) {
		new Term(length, units)
	}

}
