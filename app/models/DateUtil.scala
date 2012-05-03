package models

import org.joda.time._
import java.util.Date
import play.api._

/** 
	Object that does calculations on dates 
*/
object DateUtil {

	def termToPeriod(term: Term): ReadablePeriod = {
		term.units match {
			case TimePeriodUnits.DAY => {
				Days.days(term.length)
			}
			case TimePeriodUnits.MONTH => {
				Months.months(term.length)
			}
			case TimePeriodUnits.YEAR => {
				Years.years(term.length)
			}
		}
	}

	/**
		Given a contract and today, figure out the last day for this period of the contract.
		If the contract doesn't auto-renew, there is only one period and it is pretty simple.
		If the contract does auto-renew, this accounts for the auto-renewal periods.
		Note that this has no way of checking if the contract has been cancelled already.

		@param startDate When the contract starts.
		@param initialTerm The initial term of the contract
		@param autoRenewPeriod After the initial period, the period on which it auto-renews. For instance, 
			a contract may have an initial 1-year term, but renew monthly after that. Null means the
			contract does not auto renew.
		@param today Find the end of the period relative to this date. Defaults to today.
		@returns The date on which this term of the contract ends. It may be in the past if the contract
			does not auto-renew and has terminated.
	*/
	def calculateLastDay(
		startDate: LocalDate, 
		term: Term, 
		autoRenewPeriod: Option[Term], 
		today: LocalDate = LocalDate.now()
	): LocalDate = {

		val termPeriod = termToPeriod(term)
		var firstRenewalDate = startDate.plus(termPeriod)
		if (term.units != TimePeriodUnits.DAY && firstRenewalDate.getDayOfMonth != startDate.getDayOfMonth) {
			firstRenewalDate = firstRenewalDate.plus(Days.ONE)
		}
		println("Start " + startDate + ", term " + term + ", first renewal " + firstRenewalDate)
		val firstEndDate = firstRenewalDate.minus(Days.ONE)

		val daysSinceFirstEnd = Days.daysBetween(firstEndDate, today).getDays;

		Logger.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
		Logger.debug("Calculate start " + startDate + ", term " + term + ", arp " + autoRenewPeriod + ", today " + today)
		Logger.debug("First renewal " + firstRenewalDate + ", first end date "  + firstEndDate + ", " + daysSinceFirstEnd + " days since then");


		if (today.compareTo(firstEndDate) <= 0 || autoRenewPeriod == None) return firstEndDate
		else { // We have auto renewed

			val arp = autoRenewPeriod.get

			arp.units match {
				case TimePeriodUnits.DAY => {
					val renewalPeriod = Days.days(arp.length)
					val numRenewalPeriods = Days.daysBetween(firstRenewalDate, today).getDays / arp.length
					val secondRenewal = firstRenewalDate.plus(renewalPeriod)
					if (numRenewalPeriods == 0) secondRenewal.minus(Days.ONE)
					secondRenewal.plus(renewalPeriod.multipliedBy(numRenewalPeriods)).minus(Days.ONE)
				}
				case TimePeriodUnits.MONTH => {
					val renewalPeriod = Months.months(arp.length)
					val numRenewalPeriods = Months.monthsBetween(firstRenewalDate, today).getMonths / arp.length
					var secondRenewal = firstRenewalDate.plus(renewalPeriod)
					if (secondRenewal.getDayOfMonth != firstRenewalDate.getDayOfMonth) {
						secondRenewal= secondRenewal.plus(Days.ONE)
					}
					Logger.debug("rp " + renewalPeriod + " nrp " + numRenewalPeriods + " sr " + secondRenewal)
					Logger.debug(secondRenewal.plus(renewalPeriod.multipliedBy(numRenewalPeriods)).minus(Days.ONE).toString)
					secondRenewal.plus(renewalPeriod.multipliedBy(numRenewalPeriods)).minus(Days.ONE);
				}
				case TimePeriodUnits.YEAR => {
					val renewalPeriod = Years.years(arp.length)
					val numRenewalPeriods = Years.yearsBetween(firstRenewalDate, today).getYears / arp.length
					var secondRenewal = firstRenewalDate.plus(renewalPeriod)
					if (secondRenewal.getDayOfMonth != firstRenewalDate.getDayOfMonth) {
						secondRenewal= secondRenewal.plus(Days.ONE)
					}
					Logger.debug("rp " + renewalPeriod + " nrp " + numRenewalPeriods + " sr " + secondRenewal)
					Logger.debug(secondRenewal.plus(renewalPeriod.multipliedBy(numRenewalPeriods)).minus(Days.ONE).toString)
					secondRenewal.plus(renewalPeriod.multipliedBy(numRenewalPeriods)).minus(Days.ONE)
				}
			}
		}
	}

}
