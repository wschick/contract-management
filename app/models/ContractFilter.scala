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
	contractTypeIds: List[Long] = List[Long]()
	) 
{

	def makeConditionString(l: List[String], operator: String): String = {
		if (l.isEmpty) return ""
		else l.reduceLeft[String]{(list, item) => item + " " + operator + " " + list}
	}

	def sqlCondition: String = {
		println(">>>>> Making SQL condition >>>>>>")
		var conditionList = List[String]();
		//if (showOk) { conditionList ::= " status=" + OK.value }
		//if (showNearWarning) { conditionList = "status=" + NEARWARNING.value :: conditionList }
		//if (showFarWarning) { conditionList = "status=" + FARWARNING.value :: conditionList }
		//if (showTooLate) { conditionList = "status=" + TOOLATE.value :: conditionList }

		// Need to OR the show conditions, and AND everything else, so figure out the OR condition now.
		//println(">>>>>>>>>>>>>>>>>After initial conditions:  " + conditionList)
		//conditionList = List("(" + makeConditionString(conditionList, "OR") + ")")
		//println(">>>>>>>>>>>>>>>>>Make into list:  " + conditionList)

		// Make contract type condition
		println("Contract type ids: " + contractTypeIds);
		if (contractTypeIds.length > 0) {
			contractTypeIds.foreach(contractTypeId => conditionList ::= " contract_type_id=" + contractTypeId)
			conditionList = List("(" + makeConditionString(conditionList, "OR") + ")")
		}
		

		if (!showActive) { println("don't show showactive"); conditionList = "cancelled_date IS NOT NULL" :: conditionList }
		if (!showCancelled) { println("don't show cancelled"); conditionList = "cancelled_date IS NULL" :: conditionList }
		earliestStartDate.map(date => conditionList ::= "start_date>'" + date + "'")
		println("latest start date " + latestStartDate)
		latestStartDate.map(date => conditionList ::= "start_date<'" + date + "'")
		println("The final coniditon list is " + conditionList)

		val conditionString = makeConditionString(conditionList, "AND")
		println("Condition string: " + conditionString)
		if (conditionString != "") return "WHERE " + conditionString
		else return ""
	}
	  
}
