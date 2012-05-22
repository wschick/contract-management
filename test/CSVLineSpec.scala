import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import org.joda.time._

import models.CSVLine
import models.DateOrMTM
import models.MaybeTerm
import models.Term
import models.TimePeriodUnits

class CSVLineSpec extends Specification {

	"Site string parsing" should {

		"Handle a single word" in {
			val (a, z) = CSVLine.parseSites("CHI1")
			a must be equalTo "CHI1"
			z must be equalTo None
		}

		"Handle a dash" in {
			val (a, z) = CSVLine.parseSites("LAX-CHI")
			a must be equalTo "LAX"
			z must be equalTo Some("CHI")
		}

		"Handle a dash with spaces" in {
			val (a, z) = CSVLine.parseSites("LAX - CHI")
			a must be equalTo "LAX"
			z must be equalTo Some("CHI")
		}

		"Handle a slash" in {
			val (a, z) = CSVLine.parseSites("LAX/CHI")
			a must be equalTo "LAX"
			z must be equalTo Some("CHI")
		}
	}

	"Cost parsing" should {

		"Handle $48,000" in {
			val (value, currency) = CSVLine.parseCost("$48,000", "test", 0)
			value must be equalTo 48000.0
			currency must be equalTo None
		}

		"Handle $ 48,000.34" in {
			val (value, currency) = CSVLine.parseCost("$ 48,000.34", "test", 1)
			value must be equalTo 48000.34
			currency must be equalTo None
		}

		"Handle \" $ 1,000\"" in {
			val (value, currency) = CSVLine.parseCost("\" $ 1,000 \"", "test", 2)
			value must be equalTo 1000.0
			currency must be equalTo None
		}

		"Handle $- " in {
			val (value, currency) = CSVLine.parseCost(" $-  ", "test", 3)
			value must be equalTo 0.0
			currency must be equalTo None
		}

		"Handle $ -" in {
			val (value, currency) = CSVLine.parseCost("$ -", "test", 4)
			value must be equalTo 0.0
			currency must be equalTo None
		}

		"Handle $4,000.00 AUD" in {
			val (value, currency) = CSVLine.parseCost("$4,000 AUD", "test", 5)
			value must be equalTo 4000.0
			currency must be equalTo Some("AUD")
		}

		"Handle n/a" in {
			val (value, currency) = CSVLine.parseCost("n/a", "test", 6)
			value must be equalTo 0.0
			currency must be equalTo None
		}

		"Handle 420" in {
			val (value, currency) = CSVLine.parseCost("420", "test", 7)
			value must be equalTo 420.0
			currency must be equalTo None
		}

		"Handle EUR 100.00" in {
			val (value, currency) = CSVLine.parseCost("EUR 100.00", "test", 8)
			value must be equalTo 100.0
			currency must be equalTo Some("EUR")
		}

		"Handle DKK 430.00" in {
			val (value, currency) = CSVLine.parseCost("DKK 430.00", "test", 9)
			value must be equalTo 430.0
			currency must be equalTo Some("DKK")
		}

		"Handle GBP 293.33" in {
			val (value, currency) = CSVLine.parseCost("GBP 293.33", "test", 10)
			value must be equalTo 293.33
			currency must be equalTo Some("GBP")
		}

		"Handle AUD 2309.00" in {
			val (value, currency) = CSVLine.parseCost("AUD 2309", "test", 11)
			value must be equalTo 2309.0
			currency must be equalTo Some("AUD")
		}

		"Handle AUD 2309.00 EUR" in {
			val (value, currency) = CSVLine.parseCost("AUD 2309 EUR", "test", 12)
			value must be equalTo 2309.0
			currency must be equalTo Some("???")
		}

		"Handle bad value" in {
			CSVLine.parseCost("AUD", "test", 13) must throwA[MatchError]
		}

	}

	"getString" should {

		"Handle no quotes" in {
			CSVLine.trimString("abc def") must be equalTo "abc def"
		}

		"Handle enclosing double quotes" in {
			CSVLine.trimString("\"abcdef\"") must be equalTo "abcdef"
		}

		"Handle surrounding whitespace" in {
			CSVLine.trimString(" abc def\t \t") must be equalTo "abc def"
		}

		"Handle surrounding quotes and whitespace" in {
			CSVLine.trimString("\" abc def\t \t\"") must be equalTo "abc def"
		}
	}

	"CSV line parsing" should {

		"Handle a straightforward line" in {
			val d = CSVLine.parseLine(
""""USA"	"ATT"	"Circuit - SJDAE"	1	"CHI - LAX"	1/16/2012	1/15/2013	1/16/2013	11/16/2012	"12 Months"	60	" Pro-rated Amount "	0	" $-   "	" $1,000 "	" $12,000 "	"John Doe"	"Scott McGreggor"	"mcg@att.com"		""", "nofile", 1)
			d.country must be equalTo "USA"
			d.vendor must be equalTo "ATT"
			d.lines must be equalTo "Circuit - SJDAE"
			d.quantity must be equalTo 1
			d.aSite must be equalTo "CHI"
			d.zSite must be equalTo Some("LAX")
			d.startDate.date must be equalTo Some(new LocalDate(2012,1,16))
			d.startDate.isMTM must be equalTo false
			d.startDate.error must be equalTo None
			d.endDate.date must be equalTo Some(new LocalDate(2013,1,15))
			d.endDate.isMTM must be equalTo false
			d.endDate.error must be equalTo None
			d.renewalDate.date must be equalTo Some(new LocalDate(2013,1,16))
			d.renewalDate.isMTM must be equalTo false
			d.renewalDate.error must be equalTo None
			d.earliestCancellationDate.date must be equalTo Some(new LocalDate(2012,11,16))
			d.earliestCancellationDate.isMTM must be equalTo false
			d.earliestCancellationDate.error must be equalTo None
			d.contractTerm.term must be equalTo Some(Term(12, TimePeriodUnits.MONTH))
			d.contractTerm.other must be equalTo None
			d.earliestCancellationNotice.term must be equalTo Some(Term(60, TimePeriodUnits.DAY))
			d.earliestCancellationNotice.other must be equalTo None
			d.fineForEarlyCancel must be equalTo "Pro-rated Amount"
			d.monthlyCostInFX must be equalTo "0"
			d.nrc must be equalTo 0.0
			d.mrc must be equalTo 1000.0
			d.currency must be equalTo "USD"
			d.ourContact must be equalTo "John Doe"
			d.vendorContact must be equalTo "Scott McGreggor"
			d.vendorEmail must be equalTo "mcg@att.com"
			d.digitalContract must be equalTo false
			d.physicalOriginal must be equalTo false
		}

		"Handle another straightforward line" in {
			val d = CSVLine.parseLine(
""""USA"	"Some carrier"	"Circuit - SJDAE"	99	"CHI"	1/16/2012	1/15/2013	1/16/2013	11/16/2012	"1 Year"	"4 months"	" Pro-rated Amount "	43.5	" 123.3 AUD   "	" $1,000 "	" $12,000 "	"John Doe"	"Scott McGreggor"	"mcg@att.com"	No	Yes""", "nofile", 2)
			d.country must be equalTo "USA"
			d.vendor must be equalTo "Some carrier"
			d.lines must be equalTo "Circuit - SJDAE"
			d.quantity must be equalTo 99
			d.aSite must be equalTo "CHI"
			d.zSite must be equalTo None
			d.startDate.date must be equalTo Some(new LocalDate(2012,1,16))
			d.startDate.isMTM must be equalTo false
			d.startDate.error must be equalTo None
			d.endDate.date must be equalTo Some(new LocalDate(2013,1,15))
			d.endDate.isMTM must be equalTo false
			d.endDate.error must be equalTo None
			d.renewalDate.date must be equalTo Some(new LocalDate(2013,1,16))
			d.renewalDate.isMTM must be equalTo false
			d.renewalDate.error must be equalTo None
			d.earliestCancellationDate.date must be equalTo Some(new LocalDate(2012,11,16))
			d.earliestCancellationDate.isMTM must be equalTo false
			d.earliestCancellationDate.error must be equalTo None
			d.contractTerm.term must be equalTo Some(Term(1, TimePeriodUnits.YEAR))
			d.contractTerm.other must be equalTo None
			d.earliestCancellationNotice.term must be equalTo Some(Term(4, TimePeriodUnits.MONTH))
			d.earliestCancellationNotice.other must be equalTo None
			d.fineForEarlyCancel must be equalTo "Pro-rated Amount"
			d.monthlyCostInFX must be equalTo "43.5"
			d.nrc must be equalTo 123.3
			d.mrc must be equalTo 1000.0
			d.currency must be equalTo "AUD"
			d.ourContact must be equalTo "John Doe"
			d.vendorContact must be equalTo "Scott McGreggor"
			d.vendorEmail must be equalTo "mcg@att.com"
			d.digitalContract must be equalTo false
			d.physicalOriginal must be equalTo true
		}

		"Handle M-T-M in the dates" in {
			val d = CSVLine.parseLine(
""""USA"	"Some carrier"	"Circuit - SJDAE"	99	"CHI"	1/16/2012	M-T-M	M-T-M	M-T-M	"1 Year"	"4 months"	" Pro-rated Amount "	43.5	" 123.3 AUD   "	" $1,000 "	" $12,000 "	"John Doe"	"Scott McGreggor"	"mcg@att.com"	yes	Yes""", "nofile", 3)
			d.country must be equalTo "USA"
			d.vendor must be equalTo "Some carrier"
			d.lines must be equalTo "Circuit - SJDAE"
			d.quantity must be equalTo 99
			d.aSite must be equalTo "CHI"
			d.zSite must be equalTo None
			d.startDate.date must be equalTo Some(new LocalDate(2012,1,16))
			d.startDate.isMTM must be equalTo false
			d.startDate.error must be equalTo None
			d.endDate.date must be equalTo None
			d.endDate.isMTM must be equalTo true
			d.endDate.error must be equalTo None
			d.renewalDate.date must be equalTo None
			d.renewalDate.isMTM must be equalTo true
			d.renewalDate.error must be equalTo None
			d.earliestCancellationDate.date must be equalTo None
			d.earliestCancellationDate.isMTM must be equalTo true
			d.earliestCancellationDate.error must be equalTo None
			d.contractTerm.term must be equalTo Some(Term(1, TimePeriodUnits.YEAR))
			d.contractTerm.other must be equalTo None
			d.earliestCancellationNotice.term must be equalTo Some(Term(4, TimePeriodUnits.MONTH))
			d.earliestCancellationNotice.other must be equalTo None
			d.fineForEarlyCancel must be equalTo "Pro-rated Amount"
			d.monthlyCostInFX must be equalTo "43.5"
			d.nrc must be equalTo 123.3
			d.mrc must be equalTo 1000.0
			d.currency must be equalTo "AUD"
			d.ourContact must be equalTo "John Doe"
			d.vendorContact must be equalTo "Scott McGreggor"
			d.vendorEmail must be equalTo "mcg@att.com"
			d.digitalContract must be equalTo true
			d.physicalOriginal must be equalTo true
		}

		"Handle sample line 4" in {
			val d = CSVLine.parseLine(
""""USA"	"CME "	"Circuit - XC"	9	"Intra Building XC"	2/13/2010	"M-T-M"	"M-T-M"	"M-T-M"	"M-T-M"	30	" Pro-rated Amount "	0	" $50.00 "	" $150 "	" $3,210 "	"Charlie Chaplin"	"Inky Darkness"	"Inky.Darkness@cmegroup.com"		""", "nofile", 4)
			d.country must be equalTo "USA"
			d.vendor must be equalTo "CME"
			d.lines must be equalTo "Circuit - XC"
			d.quantity must be equalTo 9
			d.aSite must be equalTo "Intra Building XC"
			d.zSite must be equalTo None
			d.startDate.date must be equalTo Some(new LocalDate(2010,2,13))
			d.startDate.isMTM must be equalTo false
			d.startDate.error must be equalTo None
			d.endDate.date must be equalTo None
			d.endDate.isMTM must be equalTo true
			d.endDate.error must be equalTo None
			d.renewalDate.date must be equalTo None
			d.renewalDate.isMTM must be equalTo true
			d.renewalDate.error must be equalTo None
			d.earliestCancellationDate.date must be equalTo None
			d.earliestCancellationDate.isMTM must be equalTo true
			d.earliestCancellationDate.error must be equalTo None
			d.contractTerm.term must be equalTo None
			d.contractTerm.other must be equalTo Some("M-T-M")
			d.earliestCancellationNotice.term must be equalTo Some(Term(30, TimePeriodUnits.DAY))
			d.earliestCancellationNotice.other must be equalTo None
			d.fineForEarlyCancel must be equalTo "Pro-rated Amount"
			d.monthlyCostInFX must be equalTo "0"
			d.nrc must be equalTo 50.0
			d.mrc must be equalTo 150.0
			d.currency must be equalTo "USD"
			d.ourContact must be equalTo "Charlie Chaplin"
			d.vendorContact must be equalTo "Inky Darkness"
			d.vendorEmail must be equalTo "Inky.Darkness@cmegroup.com"
			d.digitalContract must be equalTo false
			d.physicalOriginal must be equalTo false
		}

		"Handle sample line 18" in {
			val d = CSVLine.parseLine(
""""USA"	"Zow"	"PRI - Phone Line "	1	"HTR4"	9/20/2000	9/19/2001	9/20/2001	6/20/2001	"12 Months"	90	" Pro-rated Amount "	"EUR 305.00"	" EUR 910.00 "	" $401 "	" $4,815 "	"Joe"	"Hank "	"hank@zow.com"		""", "nofile", 5)
			d.country must be equalTo "USA"
			d.vendor must be equalTo "Zow"
			d.lines must be equalTo "PRI - Phone Line"
			d.quantity must be equalTo 1
			d.aSite must be equalTo "HTR4"
			d.zSite must be equalTo None
			d.startDate.date must be equalTo Some(new LocalDate(2000,9,20))
			d.startDate.isMTM must be equalTo false
			d.startDate.error must be equalTo None
			d.endDate.date must be equalTo Some(new LocalDate(2001,9,19))
			d.endDate.isMTM must be equalTo false
			d.endDate.error must be equalTo None
			d.renewalDate.date must be equalTo Some(new LocalDate(2001,9,20))
			d.renewalDate.isMTM must be equalTo false
			d.renewalDate.error must be equalTo None
			d.earliestCancellationDate.date must be equalTo Some(new LocalDate(2001,6,20))
			d.earliestCancellationDate.isMTM must be equalTo false
			d.earliestCancellationDate.error must be equalTo None
			d.contractTerm.term must be equalTo Some(Term(12, TimePeriodUnits.MONTH))
			d.contractTerm.other must be equalTo None
			d.earliestCancellationNotice.term must be equalTo Some(Term(90, TimePeriodUnits.DAY))
			d.earliestCancellationNotice.other must be equalTo None
			d.fineForEarlyCancel must be equalTo "Pro-rated Amount"
			d.monthlyCostInFX must be equalTo "EUR 305.00"
			d.nrc must be equalTo 910.0
			d.mrc must be equalTo 401.0
			d.currency must be equalTo "EUR"
			d.ourContact must be equalTo "Joe"
			d.vendorContact must be equalTo "Hank"
			d.vendorEmail must be equalTo "hank@zow.com"
			d.digitalContract must be equalTo false
			d.physicalOriginal must be equalTo false
		}

		"Handle sample line 49" in {
			val d = CSVLine.parseLine(
""""USA"	"Verizon"	"01234567"	1	"ORD3 "	4/04/2003	"M-T-M"	"M-T-M"	"M-T-M"	"M-T-M"	"7 Days"	0	0	" $-   "	" $155 "	" $1,860 "	"Tom"	18997499500	"some.person@verizon.com"		""", "nofile", 6)
			d.country must be equalTo "USA"
			d.vendor must be equalTo "Verizon"
			d.lines must be equalTo "01234567"
			d.quantity must be equalTo 1
			d.aSite must be equalTo "ORD3"
			d.zSite must be equalTo None
			d.startDate.date must be equalTo Some(new LocalDate(2003,4,04))
			d.startDate.isMTM must be equalTo false
			d.startDate.error must be equalTo None
			d.endDate.date must be equalTo None
			d.endDate.isMTM must be equalTo true
			d.endDate.error must be equalTo None
			d.renewalDate.date must be equalTo None
			d.renewalDate.isMTM must be equalTo true
			d.renewalDate.error must be equalTo None
			d.earliestCancellationDate.date must be equalTo None
			d.earliestCancellationDate.isMTM must be equalTo true
			d.earliestCancellationDate.error must be equalTo None
			d.contractTerm.term must be equalTo None
			d.contractTerm.other must be equalTo Some("M-T-M")
			d.earliestCancellationNotice.term must be equalTo Some(Term(7, TimePeriodUnits.DAY))
			d.earliestCancellationNotice.other must be equalTo None
			d.fineForEarlyCancel must be equalTo "0"
			d.monthlyCostInFX must be equalTo "0"
			d.nrc must be equalTo 0.0
			d.mrc must be equalTo 155.0
			d.currency must be equalTo "USD"
			d.ourContact must be equalTo "Tom"
			d.vendorContact must be equalTo "18997499500"
			d.vendorEmail must be equalTo "some.person@verizon.com"
			d.digitalContract must be equalTo false
			d.physicalOriginal must be equalTo false
		}

		"Handle sample line 5, ASI" in {
			val d = CSVLine.parseLine(
""""Australia"	"Deb - Rod"				4/4/2004	4/4/2005	"n/a"	"n/a"	"12 months"	0	0	"$1.25 AUD"	0	"n/a"	"n/a"	"Tom"	"?"	"?"	"Yes"	"no"""", "nofile", 7)
			d.country must be equalTo "Australia"
			d.vendor must be equalTo "Deb - Rod"
			d.lines must be equalTo ""
			d.quantity must be equalTo 0
			d.aSite must be equalTo ""
			d.zSite must be equalTo None
			d.startDate.date must be equalTo Some(new LocalDate(2004,4,04))
			d.startDate.isMTM must be equalTo false
			d.startDate.error must be equalTo None
			d.endDate.date must be equalTo Some(new LocalDate(2005,4,04))
			d.endDate.isMTM must be equalTo false
			d.endDate.error must be equalTo None
			d.renewalDate.date must be equalTo None
			d.renewalDate.isMTM must be equalTo false
			d.renewalDate.error must be equalTo Some("n/a")
			d.earliestCancellationDate.date must be equalTo None
			d.earliestCancellationDate.isMTM must be equalTo false
			d.earliestCancellationDate.error must be equalTo Some("n/a")
			d.contractTerm.term must be equalTo Some(Term(12, TimePeriodUnits.MONTH))
			d.contractTerm.other must be equalTo None
			d.earliestCancellationNotice.term must be equalTo Some(Term(0, TimePeriodUnits.DAY))
			d.earliestCancellationNotice.other must be equalTo None
			d.fineForEarlyCancel must be equalTo "0"
			d.monthlyCostInFX must be equalTo "$1.25 AUD"
			d.nrc must be equalTo 0.0
			d.mrc must be equalTo 0.0
			d.currency must be equalTo "USD"
			d.ourContact must be equalTo "Tom"
			d.vendorContact must be equalTo "?"
			d.vendorEmail must be equalTo "?"
			d.digitalContract must be equalTo true
			d.physicalOriginal must be equalTo false
		}

		"Handle line with bad data" in {
			CSVLine.parseLine(
""""Australia"	"Deb - Rod"				4/4/2004	4/4/2005	"n/a"	"n/a"	"12 months"	0	0	"$1.25 AUD"	0	"don't know"	"n/a"	"Tom"	"?"	"?"	"Yes"	"no"""", "nofile", 8) must throwA[MatchError]
		}
	}
}
