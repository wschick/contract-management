package models
import org.joda.time._

case class ContractFilter(
	showOk: Boolean = true, 
	showNearWarning: Boolean = true, 
	showFarWarning: Boolean = true,
	showTooLate: Boolean = true,
	showActive: Boolean = true, // Active contracts
	showCancelled: Boolean = false, // Cancelled contracts
  showM2M: Boolean = true, // Cancelled contracts
	earliestStartDate: Option[LocalDate] = None,
	latestStartDate: Option[LocalDate] = None,
	contractTypeIds: OptionList[Long] = new OptionList,
	vendorIds: OptionList[Long] = new OptionList,
	budgetIds: OptionList[Long] = new OptionList,
	locationIds: OptionList[Long] = new OptionList,
	showMSA: Option[Long] = None,
	vendorContractIdMatches: Option[String] = None, // Substring of vendor contract id
	extraInfoMatches: Option[String] = None, // Substring of extra info field
	maximumDaysToCancel: Option[Int] = None
	)
{
  def toStatusSet:Set[ContractStatus] = {
    var statusSet = Set[ContractStatus]()
    if(showOk)
      statusSet = statusSet+OK
    if(showNearWarning)
      statusSet = statusSet + NEARWARNING
    if(showFarWarning)
      statusSet = statusSet + FARWARNING
    if(showTooLate)
      statusSet = statusSet + TOOLATE
    if(showCancelled)
      statusSet = statusSet + CANCELLED
    if(showM2M)
      statusSet = statusSet + MONTH2MONTH
    if(showActive)
      statusSet = statusSet + OK + NEARWARNING + FARWARNING + TOOLATE
    return statusSet
  }
}

object ContractFilter {
}

/**
	Class handy for creating SQL conditions based on a list of values. If you have a list
	of values (v1, v2, v3), and specify an operator like "OR", then this creates a string
	"v1 OR v2 OR v3". Typically the values are possible values in a database column, and we
	want to prefix the values with the column name, e.g. "foo=". That would yield
	"foo=v1 OR foo=v2 OR foo=v3"

	@param aList An option list of values. The toString method on the values must give you the strings
	   you want to see in the final result.
	@param prefix Any prefix that should appear before each value in the string.

	*/
class OptionList[T](aList: Option[List[T]] = None, prefix: String = "") {

	val list = aList

	/**
		@param operator What to put between the prefixed strings. Typically a boolean like OR or AND
		@param actualPrefix Use if you need to override the default prefix.
		@return A string concatenating the prefix with each value, and then putting the operator between
		those strings, or None if there is no condition.
	*/
	def makeConditionString(operator: String = "OR", actualPrefix: String = prefix): Option[String] = {
		list.map(l => {
			// l is now a list of something
			if (l.isEmpty) return None
			// Make list of prefixed strings, then reduceLeft to get operator between them.
			//val stringList = l.map(actualPrefix + _.toString)
			//Some(stringList.reduceLeft[String]{(str, item) => str + " " + operator + " " + item})
			Some(l.map(actualPrefix + _.toString).mkString(" " + operator + " "))
		}).getOrElse(None)
	}
}
