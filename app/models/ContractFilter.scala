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
	showOk: Boolean, 
	showNearWarning: Boolean, 
	showFarWarning: Boolean,
	showTooLate: Boolean,
	showActive: Boolean, // Active contracts
	showCancelled: Boolean, // Cancelled contracts
	earliestStartDate: Option[LocalDate],
	latestStartDate: Option[LocalDate]
	) 
{

	def makeConditionString(l: List[String], operator: String): String = {
		if (l.length == 0) return ""
		else l.tail.foldLeft(l.head)((list, item) => item + " " + operator + " " + list)
	}

	def sqlCondition: String = {
		var conditionList = List("");
		if (showOk) { conditionList = " status=" + OK.value :: conditionList }
		if (showNearWarning) { conditionList = "status=" + NEARWARNING.value :: conditionList }
		if (showFarWarning) { conditionList = "status=" + FARWARNING.value :: conditionList }
		if (showTooLate) { conditionList = "status=" + TOOLATE.value :: conditionList }

		// Need to OR the show conditions, and AND everything else, so figure out the OR condition now.
		conditionList = List(makeConditionString(conditionList, "OR"))

		if (!showActive) { conditionList = "cancelled_date IS NULL" :: conditionList }
		if (!showCancelled) { conditionList = "cancelled_date IS NOT NULL" :: conditionList }
		earliestStartDate.map(date => conditionList = "start_date>'" + date + "'" :: conditionList)
		latestStartDate.map(date => conditionList = "start_date<'" + date + "'" :: conditionList)

		makeConditionString(conditionList, "AND")

	}
	  
}
