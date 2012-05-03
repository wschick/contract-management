import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import org.joda.time._

import models.DateUtil
import models.Term
import models.TimePeriodUnits

class DateUtilTest extends Specification {

	"Last day calculation for no renewal period" should {

		//============ NO RENEWAL PERIOD =====================

		"be Jan 30, 2000 on January 10" in {
			val startDate = new LocalDate(2000, 1, 1)
			val initialTerm = Term(30, TimePeriodUnits.DAY)
			val autoRenewalPeriod = None
			val today = new LocalDate(2000, 1, 10)
			val lastDay = DateUtil.calculateLastDay(startDate, initialTerm, autoRenewalPeriod, today)
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(1) 
			lastDay.getDayOfMonth must be equalTo(30)
		}

		"be Jan 30, 2000 on Fanuary 10" in {
			val startDate = new LocalDate(2000, 1, 1)
			val initialTerm = Term(30, TimePeriodUnits.DAY)
			val autoRenewalPeriod = None
			val today = new LocalDate(2000, 2, 10)
			val lastDay = DateUtil.calculateLastDay(startDate, initialTerm, autoRenewalPeriod, today)
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(1) 
			lastDay.getDayOfMonth must be equalTo(30)
		}

	}


	"Last day calculation for days renewal period" should {

		//============ DAYS RENEWAL PERIOD =====================

		"be Jan 30, 2000 on January 10" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term30day = Term(30, TimePeriodUnits.DAY)
			val arp10day = Some(Term(10, TimePeriodUnits.DAY))
			val lastDay = DateUtil.calculateLastDay(startDate, term30day, arp10day, new LocalDate(2000, 1, 10))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(1) 
			lastDay.getDayOfMonth must be equalTo(30)
		}

		"be Jan 30, 2000 on January 29" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term30day = Term(30, TimePeriodUnits.DAY)
			val arp10day = Some(Term(10, TimePeriodUnits.DAY))
			val lastDay = DateUtil.calculateLastDay(startDate, term30day, arp10day, new LocalDate(2000, 1, 29))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(1) 
			lastDay.getDayOfMonth must be equalTo(30)
		}

		"be Jan 30, 2000 on January 30" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term30day = Term(30, TimePeriodUnits.DAY)
			val arp10day = Some(Term(10, TimePeriodUnits.DAY))
			val lastDay = DateUtil.calculateLastDay(startDate, term30day, arp10day, new LocalDate(2000, 1, 30))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(1) 
			lastDay.getDayOfMonth must be equalTo(30)
		}

		"be Feb 9, 2000 on January 31" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term30day = Term(30, TimePeriodUnits.DAY)
			val arp10day = Some(Term(10, TimePeriodUnits.DAY))
			val lastDay = DateUtil.calculateLastDay(startDate, term30day, arp10day, new LocalDate(2000, 1, 31))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(9)
		}

		"be Feb 29, 2000 on Feb 29 with a 10 day renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term30day = Term(30, TimePeriodUnits.DAY)
			val arp10day = Some(Term(10, TimePeriodUnits.DAY))
			val lastDay = DateUtil.calculateLastDay(startDate, term30day, arp10day, new LocalDate(2000, 2, 29))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(29)
		}
		
		"be March 10, 2000 on March 10 with a 10 day renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term30day = Term(30, TimePeriodUnits.DAY)
			val arp10day = Some(Term(10, TimePeriodUnits.DAY))
			val lastDay = DateUtil.calculateLastDay(startDate, term30day, arp10day, new LocalDate(2000, 3, 10))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(10)
		}

		"be March 20, 2000 on March 11 with a 10 day renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term30day = Term(30, TimePeriodUnits.DAY)
			val arp10day = Some(Term(10, TimePeriodUnits.DAY))
			val lastDay = DateUtil.calculateLastDay(startDate, term30day, arp10day, new LocalDate(2000, 3, 11))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(20)
		}

		"be March 30, 2000 (leap year) on March 10 with a 30 day renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term30day = Term(30, TimePeriodUnits.DAY)
			val arp30day = Some(Term(30, TimePeriodUnits.DAY))
			val lastDay = DateUtil.calculateLastDay(startDate, term30day, arp30day, new LocalDate(2000, 3, 10))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(30)
		}

		"be March 29, 2001 (non-leap year) on March 10 with a 30 day renewal" in {
			val startDate = new LocalDate(2001, 1, 1)
			val term30day = Term(30, TimePeriodUnits.DAY)
			val arp30day = Some(Term(30, TimePeriodUnits.DAY))
			val lastDay = DateUtil.calculateLastDay(startDate, term30day, arp30day, new LocalDate(2001, 3, 10))
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(31)
		}
	}

	//============ DAYS WHERE RENEWAL IS ON LEAP DAY  =====================

	"Last day calculation when end of initial term falls on leap day" should {

		"be Feb 29 for contract yet to start" in {
			val startDate = new LocalDate(2000, 2, 21)
			val term = Term(9, TimePeriodUnits.DAY)
			val arp = Some(Term(10, TimePeriodUnits.DAY))
			val today = new LocalDate(2000, 1, 1)
			val lastDay = DateUtil.calculateLastDay(startDate, term, arp, today)
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(29)
		}

		"be Feb 29 for initial term" in {
			val startDate = new LocalDate(2000, 2, 21)
			val term = Term(9, TimePeriodUnits.DAY)
			val arp = Some(Term(10, TimePeriodUnits.DAY))
			val today = new LocalDate(2000, 2, 23)
			val lastDay = DateUtil.calculateLastDay(startDate, term, arp, today)
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(29)
		}

		"be Mar 10 for second term" in {
			val startDate = new LocalDate(2000, 2, 21)
			val term = Term(9, TimePeriodUnits.DAY)
			val arp = Some(Term(10, TimePeriodUnits.DAY))
			val today = new LocalDate(2000, 3, 2)
			val lastDay = DateUtil.calculateLastDay(startDate, term, arp, today)
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(10)
		}
	}


	//============ 1 MONTH TERM, 1 MONTH RENEWAL PERIOD =====================

	"Last day calculation for 1 month term, 1 month renewal period" should {

		"be Feb 29, 2000 (leap year) on Feb 10 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term1month = Term(1, TimePeriodUnits.MONTH)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term1month, arp1month, new LocalDate(2000, 2, 10))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(29)
		}

		"be Feb 29, 2000 (leap year) on Feb 28 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term1month = Term(1, TimePeriodUnits.MONTH)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term1month, arp1month, new LocalDate(2000, 2, 28))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(29)
		}

		"be Feb 29, 2000 (leap year) on Feb 29 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term1month = Term(1, TimePeriodUnits.MONTH)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term1month, arp1month, new LocalDate(2000, 2, 29))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(29)
		}
		
		"be March 31, 2000 (leap year) on March 1 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term1month = Term(1, TimePeriodUnits.MONTH)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term1month, arp1month, new LocalDate(2000, 3, 1))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		"be March 31, 2000 (leap year) on March 10 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term1month = Term(1, TimePeriodUnits.MONTH)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term1month, arp1month, new LocalDate(2000, 3, 10))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		"be Feb 28, 2001 (non-leap year) on Feb 10 with a 1-month renewal" in {
			val startDate = new LocalDate(2001, 1, 1)
			val term1month = Term(1, TimePeriodUnits.MONTH)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term1month, arp1month, new LocalDate(2001, 2, 10))
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(28)
		}

		"be Feb 28, 2001 (non-leap year) on Feb 28 with a 1-month renewal" in {
			val startDate = new LocalDate(2001, 1, 1)
			val term1month = Term(1, TimePeriodUnits.MONTH)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term1month, arp1month, new LocalDate(2001, 2, 28))
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(28)
		}

		"be March 31, 2001 (non-leap year) on March 1 with a 1-month renewal" in {
			val startDate = new LocalDate(2001, 1, 1)
			val term1month = Term(1, TimePeriodUnits.MONTH)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term1month, arp1month, new LocalDate(2001, 3, 1))
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		"be March 31, 2001 (non-leap year) on March 10 with a 1-month renewal" in {
			val startDate = new LocalDate(2001, 1, 1)
			val term1month = Term(1, TimePeriodUnits.MONTH)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term1month, arp1month, new LocalDate(2001, 3, 10))
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(31)
		}
	}


	//============ 2 MONTH TERM, 3 MONTH RENEWAL PERIOD =====================

	"Last day calculation for 2 month term, 3 months renewal period" should {

		"be Feb 29, 2000 (leap year) on Feb 10 with a 2 month term, 3 month renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term2month = Term(2, TimePeriodUnits.MONTH)
			val arp3month = Some(Term(3, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term2month, arp3month, new LocalDate(2000, 2, 10))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(29)
		}

		"be May 31, 2000 (leap year) on March 10 with a 2 month term, 3 month renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term2month = Term(2, TimePeriodUnits.MONTH)
			val arp3month = Some(Term(3, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term2month, arp3month, new LocalDate(2000, 3, 10))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(5) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		"be Aug 31, 2001 (leap year) on March 10 with a 2 month term, 3 month renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term2month = Term(2, TimePeriodUnits.MONTH)
			val arp3month = Some(Term(3, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term2month, arp3month, new LocalDate(2001, 7, 20))
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(8) 
			lastDay.getDayOfMonth must be equalTo(31)
		}
	}

	//============ 1 YEAR TERM, 1 YEAR RENEWAL PERIOD =====================

	"Last day calculation for 1 year term, 1 month renewal period" should {

		"be Dec 31 , 2000 (leap year) on Feb 10 with a 1-year renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1year = Some(Term(1, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1year, new LocalDate(2000, 2, 10))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(12) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		"be Dec 31 , 2000 (leap year) on Dec.31 with a 1-year renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1year = Some(Term(1, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1year, new LocalDate(2000, 12, 31))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(12) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		"be Dec 31 , 2001 (leap year) on Jan 1, 2001 with a 1-year renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1year = Some(Term(1, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1year, new LocalDate(2001, 1, 1))
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(12) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		"be Feb 27 , 2001 (leap year) on July 1, 2000 with a 1-year renewal" in {
			val startDate = new LocalDate(2000, 2, 28)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1year = Some(Term(1, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1year, new LocalDate(2000, 7, 1))
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(27)
		}

		// DateUtil starts in leap year.
		"be Feb 28 , 2001 (leap year) on July 1, 2000 with a 1-year renewal" in {
			val startDate = new LocalDate(2000, 2, 29)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1year = Some(Term(1, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1year, new LocalDate(2000, 7, 1))
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(28)
		}

		// DateUtil ends in leap year.
		"be Feb 27 , 2000 on July 1, 1999 with a 1-year renewal" in {
			val startDate = new LocalDate(1999, 2, 28)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1year = Some(Term(1, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1year, new LocalDate(1999, 7, 1))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(27)
		}
	}

	//============ 4 YEAR TERM, 2 YEAR RENEWAL PERIOD =====================
	"Last day calculation for 4 year term, 2 year renewal period" should {

		"be Dec 31 , 2003 (leap year) on Feb 10 with a 4 year term, 2 year renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term4year = Term(4, TimePeriodUnits.YEAR)
			val arp2year = Some(Term(2, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term4year, arp2year, new LocalDate(2000, 2, 10))
			lastDay.getYear must be equalTo(2003) 
			lastDay.getMonthOfYear must be equalTo(12) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		"be Dec 31 , 2003 (leap year) on Dec.31 with a 4 year term, 2 year renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term4year = Term(4, TimePeriodUnits.YEAR)
			val arp2year = Some(Term(2, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term4year, arp2year, new LocalDate(2000, 12, 31))
			lastDay.getYear must be equalTo(2003) 
			lastDay.getMonthOfYear must be equalTo(12) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		"be Dec 31 , 2003 (leap year) on Jan 1, 2001 with a 4 year term, 2 year renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term4year = Term(4, TimePeriodUnits.YEAR)
			val arp2year = Some(Term(2, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term4year, arp2year, new LocalDate(2001, 1, 1))
			lastDay.getYear must be equalTo(2003) 
			lastDay.getMonthOfYear must be equalTo(12) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		"be Feb 27 , 2004 (leap year) on July 1, 2003 with a 4 year term, 2 year renewal" in {
			val startDate = new LocalDate(2000, 2, 28)
			val term4year = Term(4, TimePeriodUnits.YEAR)
			val arp2year = Some(Term(2, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term4year, arp2year, new LocalDate(2003, 7, 1))
			lastDay.getYear must be equalTo(2004) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(27)
		}

		"be Feb 27 , 2006 (leap year) on July 1, 2004 with a 4 year term, 2 year renewal" in {
			val startDate = new LocalDate(2000, 2, 28)
			val term4year = Term(4, TimePeriodUnits.YEAR)
			val arp2year = Some(Term(2, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term4year, arp2year, new LocalDate(2004, 7, 1))
			lastDay.getYear must be equalTo(2006) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(27)
		}

		// DateUtil starts on leap day.
		"be Feb 28 , 2004 (leap year) on July 1, 2003 with a 4 year term, 2 year renewal" in {
			val startDate = new LocalDate(2000, 2, 29)
			val term4year = Term(4, TimePeriodUnits.YEAR)
			val arp2year = Some(Term(2, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term4year, arp2year, new LocalDate(2003, 7, 1))
			lastDay.getYear must be equalTo(2004) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(28)
		}

		// DateUtil starts on leap day. Renewal is also leap day.
		"be Feb 28 , 2006 (not a leap year) on July 1, 2004, starting on Feb. 29, 2000" in {
			val startDate = new LocalDate(2000, 2, 29)
			val term4year = Term(4, TimePeriodUnits.YEAR)
			val arp2year = Some(Term(2, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term4year, arp2year, new LocalDate(2004, 7, 1))
			lastDay.getYear must be equalTo(2006) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(28)
		}

		// DateUtil starts on leap day. Now it start renewing on day after leap day.
		"be Feb 29 , 2008 (not a leap year) on July 1, 2006, starting on Feb. 29, 2000" in {
			val startDate = new LocalDate(2000, 2, 29)
			val term4year = Term(4, TimePeriodUnits.YEAR)
			val arp2year = Some(Term(2, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term4year, arp2year, new LocalDate(2006, 7, 1))
			lastDay.getYear must be equalTo(2008) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(29)
		}

		// DateUtil starts on leap day.
		"be Feb 29 , 2012 (not a leap year) on July 1, 2010, starting on Feb. 29, 2000" in {
			val startDate = new LocalDate(2000, 2, 29)
			val term4year = Term(4, TimePeriodUnits.YEAR)
			val arp2year = Some(Term(2, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term4year, arp2year, new LocalDate(2010, 7, 1))
			lastDay.getYear must be equalTo(2012) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(29)
		}

		// DateUtil ends in leap year.
		"be Feb 27 , 2000 on July 1, 1999 starting on Feb. 28, 1999" in {
			val startDate = new LocalDate(1999, 2, 28)
			val term4year = Term(4, TimePeriodUnits.YEAR)
			val arp2year = Some(Term(2, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term4year, arp2year, new LocalDate(1999, 7, 1))
			lastDay.getYear must be equalTo(2003) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(27)
		}

		// DateUtil start day after leap day.
		"be Feb 29, 2004 on July 1, 2000 starting on March 1, 2000" in {
			val startDate = new LocalDate(2000, 3, 1)
			val term4year = Term(4, TimePeriodUnits.YEAR)
			val arp2year = Some(Term(2, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term4year, arp2year, new LocalDate(2000, 7, 1))
			lastDay.getYear must be equalTo(2004) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(29)
		}

		// DateUtil start day after leap day.
		"be Feb 28, 2006 on July 1, 2004 starting on March 1, 2000" in {
			val startDate = new LocalDate(2000, 3, 1)
			val term4year = Term(4, TimePeriodUnits.YEAR)
			val arp2year = Some(Term(2, TimePeriodUnits.YEAR))
			val lastDay = DateUtil.calculateLastDay(startDate, term4year, arp2year, new LocalDate(2004, 7, 1))
			lastDay.getYear must be equalTo(2006) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(28)
		}
	}

	//============ 1 YEAR TERM, 1 MONTH RENEWAL PERIOD =====================
		
	"Last day calculation for 1 year term, 1 month renewal period" should {

		// Sign contract on Feb. 28 of leap year.

		"be Feb 28, 2001 on Feb. 25, 2001 with a Feb. 28, 2000 start date" in {
			val startDate = new LocalDate(2000, 2, 28)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val today = new LocalDate(2001, 2, 25)
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, today)
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(27)
		}

		"be March 27, 2001 on Feb. 28, 2001 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 2, 28)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val today = new LocalDate(2001, 2, 28)
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, today)
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(27)
		}

		// Sign contract on Feb. 29 of leap year.

		"be Feb 28, 2001 on Feb. 25, 2001 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 2, 29)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val today = new LocalDate(2001, 2, 25)
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, today)
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(28)
		}

		"be Feb 28, 2001 on Feb. 28, 2001 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 2, 29)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val today = new LocalDate(2001, 2, 28)
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, today)
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(28)
		}

		"be March 31, 2001 on March. 1, 2001 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 2, 29)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val today = new LocalDate(2001, 3, 1)
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, today)
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(31)
		}
		
		// Sign contract on Mar. 1 of leap year.

		"be Feb 28, 2001 on Feb. 25, 2001 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 3, 1)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val today = new LocalDate(2001, 2, 25)
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, today)
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(28)
		}

		"be Feb 28, 2001 on Feb. 28, 2001 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 3, 1)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val today = new LocalDate(2001, 2, 28)
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, today)
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(28)
		}

		"be March 31, 2001 on March. 1, 2001 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 3, 1)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val today = new LocalDate(2001, 3, 1)
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, today)
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		// Sign contract on Mar. 1 of year before leap year.

		"be Feb 29, 2000 on Feb. 25, 2000 with a 1-month renewal" in {
			val startDate = new LocalDate(1999, 3, 1)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val today = new LocalDate(2000, 2, 25)
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, today)
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(29)
		}

		"be Feb 29, 2000 on Feb. 29, 2000 with a 1-month renewal" in {
			val startDate = new LocalDate(1999, 3, 1)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val today = new LocalDate(2000, 2, 25)
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, today)
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(2) 
			lastDay.getDayOfMonth must be equalTo(29)
		}

		"be March 31, 2000 on March. 1, 2000 with a 1-month renewal" in {
			val startDate = new LocalDate(1999, 3, 1)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val today = new LocalDate(2000, 3, 1)
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, today)
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		"be March 14, 2000 on Nov 3 with a 1-month renewal" in {
			val startDate = new LocalDate(1999, 3, 15)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, new LocalDate(1999, 11, 3))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(14)
		}

		"be Dec 31, 2000 on Dec 31 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, new LocalDate(2000, 12, 31))
			lastDay.getYear must be equalTo(2000) 
			lastDay.getMonthOfYear must be equalTo(12) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		// We sign contract Feb. 29. Last day will be Feb. 28, start of next period is March 1, last days is March 31
		"be March 31, 2001 on March 5, 2001 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 2, 29)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, new LocalDate(2001, 3, 5))
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(3) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

		"be Jan 31, 2001 on Jan 1, 2001 with a 1-month renewal" in {
			val startDate = new LocalDate(2000, 1, 1)
			val term1year = Term(1, TimePeriodUnits.YEAR)
			val arp1month = Some(Term(1, TimePeriodUnits.MONTH))
			val lastDay = DateUtil.calculateLastDay(startDate, term1year, arp1month, new LocalDate(2001, 1, 1))
			lastDay.getYear must be equalTo(2001) 
			lastDay.getMonthOfYear must be equalTo(1) 
			lastDay.getDayOfMonth must be equalTo(31)
		}

	}

}
