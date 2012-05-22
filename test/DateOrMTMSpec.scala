import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import org.joda.time._

import models.CSVLine
import models.DateOrMTM
import models.MaybeTerm
import models.TimePeriodUnits

class DateOrMTMSpec extends Specification {

	"date parsing" should {

		"Handle a standard date" in {
			val d = DateOrMTM.parseDate("03/04/2012")
			d.date must be equalTo (Some(new LocalDate(2012, 3, 4)))
			d.isMTM must be equalTo false
			d.error must be equalTo None
		}

		"Handle a bad date" in {
			val d = DateOrMTM.parseDate("03-04-2012")
			d.date must be equalTo None
			d.isMTM must be equalTo false
			d.error must be equalTo Some("03-04-2012")
		}

		"Handle M-T-M" in {
			val d = DateOrMTM.parseDate("M-T-M")
			d.date must be equalTo None
			d.isMTM must be equalTo true
			d.error must be equalTo None
		}

		"Handle other text" in {
			val d = DateOrMTM.parseDate("ii")
			d.date must be equalTo None
			d.isMTM must be equalTo false
			d.error must be equalTo Some("ii")
		}
	}
}
