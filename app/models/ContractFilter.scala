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
	contractTypeIds: OptionList[Long],//List[Long](),
	vendorIds: OptionList[Long],
	budgetIds: OptionList[Long],
	locationIds: OptionList[Long],
	showMSA: Option[Long],
	vendorContractIdMatches: Option[String], // Substring of vendor contract id
	extraInfoMatches: Option[String], // Substring of extra info field
	maximumDaysToCancel: Option[Int]
	) 
{


	def sqlCondition: String = {
		var conditionList = List[String]()
		var conditionString: String = ""
		//if (showOk) { conditionList ::= " status=" + OK.value }
		//if (showNearWarning) { conditionList = "status=" + NEARWARNING.value :: conditionList }
		//if (showFarWarning) { conditionList = "status=" + FARWARNING.value :: conditionList }
		//if (showTooLate) { conditionList = "status=" + TOOLATE.value :: conditionList }

		// Need to OR the show conditions, and AND everything else, so figure out the OR condition now.
		//println(">>>>>>>>>>>>>>>>>After initial conditions:  " + conditionList)
		//conditionList = List("(" + makeConditionString(conditionList, "OR") + ")")
		//println(">>>>>>>>>>>>>>>>>Make into list:  " + conditionList)

		// Make contract type condition
		conditionList :::= contractTypeIds.makeConditionString()
		conditionList :::=  vendorIds.makeConditionString()
		conditionList :::= budgetIds.makeConditionString()
		conditionList :::= locationIds.makeConditionString()
		/*println("Contract type ids: " + contractTypeIds);
		val contractTypeCondition: String = 
			contractTypeIds.map(ctis => {
				
				ctis.foreach(contractTypeId => conditionList ::= " contract_type_id=" + contractTypeId)
				conditionList = List("(" + makeConditionString(conditionList, "OR", "contact_type_id=") + ")")
			}
		

		if (!showActive) { println("don't show showactive"); conditionList = "cancelled_date IS NOT NULL" :: conditionList }
		if (!showCancelled) { println("don't show cancelled"); conditionList = "cancelled_date IS NULL" :: conditionList }
		earliestStartDate.map(date => conditionList ::= "start_date>'" + date + "'")
		println("latest start date " + latestStartDate)
		latestStartDate.map(date => conditionList ::= "start_date<'" + date + "'")
		println("The final coniditon list is " + conditionList)

		val conditionString = ContractFilter.makeConditionString(conditionList, "AND")
		println("Condition string: " + conditionString)
			*/
		if (conditionString != "") return "WHERE " + conditionString
		else return ""
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
class OptionList[T](aList: Option[List[T]], prefix: String = "") {

	val list = aList

	/**
		@param operator What to put between the prefixed strings. Typically a boolean like OR or AND
		@param actualPrefix Use if you need to override the default prefix.
		@returns A string concatenating the prefix with each value, and then putting the operator between
		those strings.
	*/
	def makeConditionString(operator: String = "OR", actualPrefix: String = prefix): List[String] = {
		list.map(l => {
			if (l.isEmpty) return Nil
			// Make list of prefixed strings, then reduceLeft to get operator between them.
			val stringList = l.map(actualPrefix + _.toString)
			List(stringList.reduceLeft[String]{(str, item) => str + " " + operator + " " + item})
		}).getOrElse(Nil
	}
}
