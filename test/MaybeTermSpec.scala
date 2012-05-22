import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import org.joda.time._

import models.CSVLine
import models.DateOrMTM
import models.MaybeTerm
import models.TimePeriodUnits

class MaybeTermSpec extends Specification {

	"term parsing" should {

		"Handle a bare number" in {
			val t = MaybeTerm.parseTerm("42")
			t.other must be equalTo None
			t.term.get.length must be equalTo 42
			t.term.get.units must be equalTo TimePeriodUnits.DAY
		}

		"Handle \"days\"" in {
			val t = MaybeTerm.parseTerm("42 days")
			t.other must be equalTo None
			t.term.get.length must be equalTo 42
			t.term.get.units must be equalTo TimePeriodUnits.DAY
		}

		"Handle \"Days\"" in {
			val t = MaybeTerm.parseTerm("42 Days")
			t.other must be equalTo None
			t.term.get.length must be equalTo 42
			t.term.get.units must be equalTo TimePeriodUnits.DAY
		}

		"Handle \"month\"" in {
			val t = MaybeTerm.parseTerm("1 month")
			t.other must be equalTo None
			t.term.get.length must be equalTo 1
			t.term.get.units must be equalTo TimePeriodUnits.MONTH
		}

		"Handle \"months\"" in {
			val t = MaybeTerm.parseTerm("42 months")
			t.other must be equalTo None
			t.term.get.length must be equalTo 42
			t.term.get.units must be equalTo TimePeriodUnits.MONTH
		}

		"Handle \"Months\"" in {
			val t = MaybeTerm.parseTerm("42 Months")
			t.other must be equalTo None
			t.term.get.length must be equalTo 42
			t.term.get.units must be equalTo TimePeriodUnits.MONTH
		}

		"Handle other text" in {
			val t = MaybeTerm.parseTerm("42 Mths")
			t.term must be equalTo None
			t.other must be equalTo Some("42 Mths") 
		}

		"Handle \"Years\"" in {
			val t = MaybeTerm.parseTerm("42 Years")
			t.other must be equalTo None
			t.term.get.length must be equalTo 42
			t.term.get.units must be equalTo TimePeriodUnits.YEAR
		}

		"Handle \"Year\"" in {
			val t = MaybeTerm.parseTerm("1 Year")
			t.other must be equalTo None
			t.term.get.length must be equalTo 1
			t.term.get.units must be equalTo TimePeriodUnits.YEAR
		}

	}

}
