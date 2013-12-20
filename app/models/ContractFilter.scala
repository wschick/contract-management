package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.data._
//import play.api.data.Forms._
//import play.api.Play.current
import org.joda.time._

/** Filter parameters for the Contract view */
case class ContractFilter(
	showOk: Boolean = true, 
	showNearWarning: Boolean = true, 
	showFarWarning: Boolean = true,
	showTooLate: Boolean = true,
	showActive: Boolean = true, // Active contracts
	showCancelled: Boolean = false, // Cancelled contracts
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


	def sqlCondition: String = {
//		var conditionList = List[String]()
//		var conditionString: String = ""
		//if (showOk) { conditionList ::= " status=" + OK.value }
		//if (showNearWarning) { conditionList = "status=" + NEARWARNING.value :: conditionList }
		//if (showFarWarning) { conditionList = "status=" + FARWARNING.value :: conditionList }
		//if (showTooLate) { conditionList = "status=" + TOOLATE.value :: conditionList }

		// Need to OR the show conditions, and AND everything else, so figure out the OR condition now.
		//Logger.debug(">>>>>>>>>>>>>>>>>After initial conditions:  " + conditionList)
		//conditionList = List("(" + makeConditionString(conditionList, "OR") + ")")
		//Logger.debug(">>>>>>>>>>>>>>>>>Make into list:  " + conditionList)

		// Make contract type condition
//		contractTypeIds.makeConditionString().map(c => conditionList +:= c)
//		vendorIds.makeConditionString().map(c => conditionList +:= c)
//		budgetIds.makeConditionString().map(c => conditionList +:= c)
//		locationIds.makeConditionString().map(c => conditionList +:= c)
		/*Logger.debug("Contract type ids: " + contractTypeIds);
		val contractTypeCondition: String = 
			contractTypeIds.map(ctis => {
				
				ctis.foreach(contractTypeId => conditionList ::= " contract_type_id=" + contractTypeId)
				conditionList = List("(" + makeConditionString(conditionList, "OR", "contact_type_id=") + ")")
			}
		

		if (!showActive) { Logger.debug("don't show showactive"); conditionList = "cancelled_date IS NOT NULL" :: conditionList }
		if (!showCancelled) { Logger.debug("don't show cancelled"); conditionList = "cancelled_date IS NULL" :: conditionList }
		earliestStartDate.map(date => conditionList ::= "start_date>'" + date + "'")
		Logger.debug("latest start date " + latestStartDate)
		latestStartDate.map(date => conditionList ::= "start_date<'" + date + "'")
		Logger.debug("The final coniditon list is " + conditionList)

		val conditionString = ContractFilter.makeConditionString(conditionList, "AND")
		Logger.debug("Condition string: " + conditionString)
			*/

    // where clause to be refactor by slick api
    var statusList = List[String]()
    if (showNearWarning){
      statusList = "(CASE term_units WHEN 0 THEN DATEDIFF(DATE_SUB(DATE_ADD(start_date,INTERVAL term DAY), INTERVAL cancellation_period DAY), CURDATE()) BETWEEN 1 AND 29 WHEN 1 THEN DATEDIFF(DATE_SUB(DATE_ADD(start_date,INTERVAL term MONTH), INTERVAL cancellation_period DAY), CURDATE()) BETWEEN 1 AND 29 ELSE DATEDIFF(DATE_SUB(DATE_ADD(start_date,INTERVAL term YEAR), INTERVAL cancellation_period DAY), CURDATE()) BETWEEN 1 AND 29 END)"::statusList
    }

    if (showFarWarning){
      statusList = "(CASE term_units WHEN 0 THEN DATEDIFF(DATE_SUB(DATE_ADD(start_date,INTERVAL term DAY), INTERVAL cancellation_period DAY), CURDATE()) BETWEEN 31 AND 59 WHEN 1 THEN DATEDIFF(DATE_SUB(DATE_ADD(start_date,INTERVAL term MONTH), INTERVAL cancellation_period DAY), CURDATE()) BETWEEN 31 AND 59 ELSE DATEDIFF(DATE_SUB(DATE_ADD(start_date,INTERVAL term YEAR), INTERVAL cancellation_period DAY), CURDATE()) BETWEEN 31 AND 59 END)"::statusList
    }

    if (showTooLate){
      statusList = "(CASE term_units WHEN 0 THEN DATEDIFF(DATE_SUB(DATE_ADD(start_date,INTERVAL term DAY), INTERVAL cancellation_period DAY), CURDATE()) <= 0 WHEN 1 THEN DATEDIFF(DATE_SUB(DATE_ADD(start_date,INTERVAL term MONTH), INTERVAL cancellation_period DAY), CURDATE()) <= 0 ELSE DATEDIFF(DATE_SUB(DATE_ADD(start_date,INTERVAL term YEAR), INTERVAL cancellation_period DAY), CURDATE()) <=0 END)"::statusList
    }

    if (showOk){
      statusList = "(CASE term_units WHEN 0 THEN DATEDIFF(DATE_SUB(DATE_ADD(start_date,INTERVAL term DAY), INTERVAL cancellation_period DAY), CURDATE()) >= 60 WHEN 1 THEN DATEDIFF(DATE_SUB(DATE_ADD(start_date,INTERVAL term MONTH), INTERVAL cancellation_period DAY), CURDATE()) >= 60 ELSE DATEDIFF(DATE_SUB(DATE_ADD(start_date,INTERVAL term YEAR), INTERVAL cancellation_period DAY), CURDATE()) >= 60  END)"::statusList
    }

    if (showActive){
      statusList = "(cancelled_date IS NULL)"::statusList
    }

    if (showCancelled){
      statusList = "(cancelled_date IS NOT NULL)"::statusList
    }

    if (statusList.size==0) return ""
    else return "WHERE " + statusList.reduceLeft((s1, s2)=> s1 + " OR " + s2)
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
