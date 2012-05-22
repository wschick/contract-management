import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import org.joda.time._

import models.OptionList

class ContractFilterSpec extends Specification {

	"makeConditionString" should {

		"Handle no list" in {
			val l = new OptionList(None, "+")
			l.makeConditionString("OR") must be equalTo(None)
		}

		"Handle an empty list" in {
			val l = new OptionList(Some(List()), "+")
			l.makeConditionString("OR") must be equalTo(None)
		}

		"Handle a single (float) item" in {
			val l = new OptionList(Some(List(9.0)), "+")
			l.makeConditionString("OR").get must be equalTo("+9.0")
		}

		"Handle a list of integers" in {
			val l = new OptionList[Int](Some(List(1,2,3,4)), "+")
			l.makeConditionString("OR").get must be equalTo("+1 OR +2 OR +3 OR +4")
		}

		"Handle a list of integers with no prefix" in {
			val l = new OptionList[Int](Some(List(1,2,3,4)))
			l.makeConditionString("OR").get must be equalTo("1 OR 2 OR 3 OR 4")
		}

		"Handle a list of strings" in {
			val l = new OptionList(Some(List("a", "b", "c")), "+")
			l.makeConditionString("OR").get must be equalTo("+a OR +b OR +c")
		}

	}

}
