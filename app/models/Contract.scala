package models

import play.api._
import play.api.db._
import play.api.Play.current
import org.joda.time._
import java.sql.{Date, Time}

import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession
import models.NEARWARNING
import com.sun.org.apache.xpath.internal.operations.And
import models.TimePeriodUnits.MONTH

case class Contract(
	id: Option[Long],
	vendor: Company,
	vendorContractId: String, // Used for filing
	billingAccount: Option[String],
	isMSA: Boolean,
	MSAId: Option[Long], // Points to contract that is MSA.
	extraInfo: Option[String], 
	description: Option[String],
	contractType: ContractType,
	aEnd: Location, 
	zEnd: Location,
	cost: ContractCosts,
	startDate: LocalDate,
	term: Term,
	cancellationPeriod: Term,
	cancelledDate: Option[LocalDate],
	autoRenewPeriod: Option[Term],
	attention: Option[String],
	lastModifyingUser: Option[String] = None,
//	lastModifiedTime: Option[LocalDateTime] = Some(LocalDateTime.now())
  lastModifiedTime: Option[LocalDateTime] = None
	) 
{
	def vendorIdString(): String = {
		Company.findById(vendor.id).get.name + " " + vendorContractId
	}

	def name(): String = vendorIdString

	def startDateStr(): String = DateUtil.format(startDate)

	def cancelledDateStr(): String = cancelledDate.map(cd => DateUtil.format(cd)).getOrElse("")

	def lastDay(): LocalDate = DateUtil.calculateLastDay(startDate, term, autoRenewPeriod)

	def lastDayStr(): String = DateUtil.format(lastDay)

	def cancellationDate(): LocalDate = {
		lastDay.minus(cancellationPeriod.period)
	}

	def cancellationDateStr(): String = DateUtil.format(cancellationDate)

	def daysUntilCancellationDate(): Int = {
		Days.daysBetween(new LocalDate(), cancellationDate()).getDays
	}

	def willAutoRenew(): Boolean = autoRenewPeriod != None

  def isM2M: Boolean = {
    if(lastDay.compareTo(new LocalDate())<0)
      false
    else{
      autoRenewPeriod match {
        case None => false
        case Some(x) => x.period.equals(Period.months(1)) || x.period.equals(Period.days(30))
      }
    }
  }

  def status(): ContractStatus = {
      cancelledDate match {
        case Some(x) => CANCELLED
        case None => {
          if(isM2M)
            MONTH2MONTH
          else
           daysUntilCancellationDate match {
             case x if (x <= 0) => TOOLATE
             case x if (x < 30) => NEARWARNING
             case x if (x < 60) => FARWARNING
             case _ => OK
           }
        }
      }
    }

	def hasAttachments: Boolean = Attachment.contractHasAttachments(Company.findById(vendor.id).get.name, vendorContractId)

	def attachments(): Seq[Attachment] = Attachment.getContractAttachments(Company.findById(vendor.id).get.name, vendorContractId)

	def lastModifiedTimeStr(): String = DateUtil.formatDT(lastModifiedTime)

  def getContract22:Contract22 = Contract22(id,
    vendor.id,
    vendorContractId,
    billingAccount,
    isMSA,
    MSAId,
    extraInfo,
    contractType.id,
    aEnd.id,
    zEnd.id,
    cost.mrc,
    cost.nrc,
    cost.currency.id,
    cost.budget.id,
    new java.sql.Date(startDate.toDate.getTime),
    term.length,
    term.units.value,
    cancellationPeriod.length,
    cancellationPeriod.units.value,
    cancelledDate.map(localDate=>Option(new java.sql.Date(localDate.toDate.getTime))).getOrElse(None),
    autoRenewPeriod.map(term=>Option(term.length)).getOrElse(None),
    autoRenewPeriod.map(term=>Option(term.units.value)).getOrElse(None))

  def getContract4:Contract4 = Contract4(id,
                                         description,
                                         attention,
                                         lastModifyingUser,
                                         lastModifiedTime.map(localDateTime=>Option(new java.sql.Time(localDateTime.toDateTime.getMillis))).getOrElse(None)
  )

//  def fromRow(id: Long, vendor_id: Long, vendor_contract_id: String, billing_account: String, is_msa: Boolean,
//              msa_id: Long, extra_info: String, description: String, contract_type_id: Long, a_end_id: Long,
//              z_end_id: Long, mrc: Double, nrc: Double, currency_id: Long, budget_id: Long,
//              start_date: Date, term: Int, term_units: Int, cancellation_period: Int, cancellation_period_units: Int,
//              cancelled_date: Date, auto_renew_period: Int, auto_renew_period_units: Int, attention: String, last_modifying_user: String,
//              last_modified_time: Date): Contract =
//    Contract(Some(id), Company.findById(vendor_id).get, vendor_contract_id, Some(billing_account), is_msa, Some(msa_id),
//             Some(extra_info), Some(description), ContractType.findById(contract_type_id).get, Location.findById(a_end_id).get, Location.findById(z_end_id).get,
//             ContractCosts.create(mrc, nrc, currency_id, budget_id), new LocalDate(start_date), Term(term, TimePeriodUnits.create(term_units)),
//             Term(cancellation_period, TimePeriodUnits.create(cancellation_period_units)),
//             cancelledDate.map(date => Option(new LocalDate(date))).getOrElse(None),
//             { if (auto_renew_period == None || auto_renew_period_units == None) None
//               else Some(Term(auto_renew_period, TimePeriodUnits.create(auto_renew_period_units))) },
//             Some(attention),
//             Some(last_modifying_user),
//             Some(new LocalDateTime(last_modified_time)))
//
//  def toRow(c: Contract) = None//Some((c.id, c.myBar.myInt, c.myBar.myString))

//    val q = for { c <- Contract22 if c.id===id} yield (c.vendor_id ~ c.vendor_contract_id ~ c.billing_account ~ c.is_msa ~
//      c.msa_id ~ c.extra_info ~ c.contract_type_id ~ c.a_end_id ~
//      c.z_end_id ~ c.mrc ~ c.nrc ~ c.currency_id ~ c.budget_id ~
//      c.start_date ~ c.term ~ c.term_units ~ c.cancellation_period ~ c.cancellation_period_units ~
//      c.cancelled_date ~ c.auto_renew_period ~ c.auto_renew_period_units)
//
//    q.update(contract22.vendor_id, contract22.vendor_contract_id, contract22.billing_account, contract22.is_msa,
//      contract22.msa_id, contract22.extra_info, contract22.contract_type_id, contract22.a_end_id,
//      contract22.z_end_id, contract22.mrc, contract22.nrc, contract22.currency_id, contract22.budget_id,
//      contract22.start_date, contract22.term, contract22.term_units, contract22.cancellation_period, contract22.cancellation_period_units,
//      contract22.cancelled_date, contract22.auto_renew_period, contract22.auto_renew_period_units)
  
}

object Contract {
  def all(): List[Contract] =  {
    for{
      c22 <- Contract22.all
      c4 <- Contract4.all
      if c22.id==c4.id
    } yield combine(Some(c22), Some(c4)).get
  }

  def combine(c22:Option[Contract22], c4:Option[Contract4]):Option[Contract] = {
    if(c22==None || c4==None) None
    else{
      val a = c22.get
      val b = c4.get
      Some(Contract(a.id, Company.findById(a.vendor_id).get, a.vendor_contract_id, a.billing_account, a.is_msa, a.msa_id,
      a.extra_info, b.description, ContractType.findById(a.contract_type_id).get, Location.findById(a.a_end_id).get, Location.findById(a.z_end_id).get,
      ContractCosts.create(a.mrc, a.nrc, a.currency_id, a.budget_id), new LocalDate(a.start_date), Term(a.term, TimePeriodUnits.create(a.term_units)),
      Term(a.cancellation_period, TimePeriodUnits.create(a.cancellation_period_units)),
      a.cancelled_date.map(date => Option(new LocalDate(date))).getOrElse(None),
      { if (a.auto_renew_period == None || a.auto_renew_period_units == None) None
      else Some(Term(a.auto_renew_period.get, TimePeriodUnits.create(a.auto_renew_period_units.get))) },
      b.attention,
      b.last_modifying_user,
      {
        if(b.last_modified_time==None) None
        else Some(new LocalDateTime(b.last_modified_time.get.getTime))
      }))
    }
  }

	def filtered(filter: ContractFilter): List[Contract] = {
    this.all().filter(contract=>filterPredicate(contract, filter))
	}

  def filterPredicate(c:Contract, filter: ContractFilter) : Boolean = {
    filter.toStatusSet.contains(c.status()) ||
    (filter.contractTypeIds.list !=None && filter.contractTypeIds.list.get.exists(_==c.contractType.id))// ||
//      ((filter.earliestStartDate!=None && filter.earliestStartDate.get.compareTo(c.startDate)<=0)&&
//       ((filter.latestStartDate!=None && filter.latestStartDate.get.compareTo(c.startDate)>=0)))
  }

	def findById(id: Long): Option[Contract] = {
    combine(Contract22.findById(id), Contract4.findById(id))
	}

	def nameById(id: Long): Option[String] = {
		findById(id).map(contract => Some(contract.name)).getOrElse(None)
	}

	def create(contract: Contract): Long = {
    val insertedId = Contract22.create(contract.getContract22)
    Contract4.update(insertedId, contract.getContract4)
    return insertedId
	}

	def update(id: Long, contract: Contract) {
    Contract22.update(id, contract.getContract22)
    Contract4.update(id, contract.getContract4)
	}
					  
	def delete(id: Long) {
    Contract4.delete(id)
	}

	def options(msaOnly: Boolean = false): Seq[(String, String)] = {
    Contract22.options(msaOnly)
  }

	def MSAOptions: Seq[(String, String)] = options(true)
}

//		get[Pk[Long]]("id") ~
//		get[Long]("vendor_id") ~
//		get[String]("vendor_contract_id") ~
//		get[Option[String]]("billing_account") ~
//		get[Boolean]("is_msa") ~
//		get[Option[Long]]("msa_id") ~
//		get[Option[String]]("extra_info") ~
//		get[Option[String]]("description") ~
//		get[Long]("contract_type_id") ~
//		get[Long]("a_end_id") ~
//		get[Long]("z_end_id") ~
//		get[Double]("mrc") ~
//		get[Double]("nrc") ~
//		get[Long]("currency_id") ~
//		get[Long]("budget_id") ~
//		get[Date]("start_date") ~
//		get[Int]("term") ~
//		get[Int]("term_units") ~
//		get[Int]("cancellation_period") ~
//		get[Int]("cancellation_period_units") ~
//		get[Option[Date]]("cancelled_date") ~
//		get[Option[Int]]("auto_renew_period") ~
//		get[Option[Int]]("auto_renew_period_units") ~
//		get[Option[String]]("attention") ~
//		get[Option[String]]("last_modifying_user") ~
//		get[Option[Date]]("last_modified_time") map {

case class Contract22(id: Option[Long], vendor_id: Long, vendor_contract_id: String, billing_account: Option[String], is_msa: Boolean,
                      msa_id: Option[Long], extra_info: Option[String], contract_type_id: Long, a_end_id: Long,
                      z_end_id: Long, mrc: Double, nrc: Double, currency_id: Long, budget_id: Long,
                      start_date: Date, term: Int, term_units: Int, cancellation_period: Int, cancellation_period_units: Int,
                      cancelled_date: Option[Date], auto_renew_period: Option[Int], auto_renew_period_units: Option[Int])

object Contract22 extends Table[Contract22]("contract") with DbUtils{
  def id = column[Long]("id", O.PrimaryKey)
  def vendor_id = column[Long]("vendor_id")
  def vendor_contract_id = column[String]("vendor_contract_id")
  def billing_account = column[String]("billing_account")
  def is_msa = column[Boolean]("is_msa")
  def msa_id = column[Long]("msa_id")
  def extra_info = column[String]("extra_info")
  def contract_type_id = column[Long]("contract_type_id")
  def a_end_id = column[Long]("a_end_id")
  def z_end_id = column[Long]("z_end_id")
  def mrc = column[Double]("mrc")
  def nrc = column[Double]("nrc")
  def currency_id = column[Long]("currency_id")
  def budget_id = column[Long]("budget_id")
  def start_date = column[Date]("start_date")
  def term = column[Int]("term")
  def term_units = column[Int]("term_units")
  def cancellation_period = column[Int]("cancellation_period")
  def cancellation_period_units = column[Int]("cancellation_period_units")
  def cancelled_date = column[Date]("cancelled_date")
  def auto_renew_period = column[Int]("auto_renew_period")
  def auto_renew_period_units = column[Int]("auto_renew_period_units")

  def * = id.? ~ vendor_id ~ vendor_contract_id ~ billing_account.? ~ is_msa ~
    msa_id.? ~ extra_info.? ~ contract_type_id ~ a_end_id ~
    z_end_id ~ mrc ~ nrc ~ currency_id ~ budget_id ~
    start_date ~ term ~ term_units ~ cancellation_period ~ cancellation_period_units ~
    cancelled_date.? ~ auto_renew_period.? ~ auto_renew_period_units.? <> (Contract22.apply _, Contract22.unapply _)

  def all(): List[Contract22] = withSession {
    Query(Contract22) list
  }

  def filtered(filter: ContractFilter): List[Contract22] = withSession {
    this.all()
    //		DB.withConnection { implicit connection =>
    //      Logger.debug("My SQL string: " + "select * from contract " + filter.sqlCondition)
    //			SQL("select * from contract " + filter.sqlCondition).as(contract *)
    //		}// and do something here to let the contract filter pick which ones are kept. Have filter method on contract filter object.
  }

  def findById(id: Long): Option[Contract22] = withSession{
    val q = for{
      c22 <- Contract22 if c22.id===id
    }yield c22
    q.firstOption
  }

  def create(contract22: Contract22): Long = withSession {
    (Contract22.vendor_id ~ Contract22.vendor_contract_id ~ Contract22.billing_account.? ~ Contract22.is_msa ~
      Contract22.msa_id.? ~ Contract22.extra_info.? ~ Contract22.contract_type_id ~ Contract22.a_end_id ~
      Contract22.z_end_id ~ Contract22.mrc ~ Contract22.nrc ~ Contract22.currency_id ~ Contract22.budget_id ~
      Contract22.start_date ~ Contract22.term ~ Contract22.term_units ~ Contract22.cancellation_period ~ Contract22.cancellation_period_units ~
      Contract22.cancelled_date.? ~ Contract22.auto_renew_period.? ~ Contract22.auto_renew_period_units.?).
      insert(
        contract22.vendor_id, contract22.vendor_contract_id, contract22.billing_account, contract22.is_msa,
        contract22.msa_id, contract22.extra_info, contract22.contract_type_id, contract22.a_end_id,
        contract22.z_end_id, contract22.mrc, contract22.nrc, contract22.currency_id, contract22.budget_id,
        contract22.start_date, contract22.term, contract22.term_units, contract22.cancellation_period, contract22.cancellation_period_units,
        contract22.cancelled_date, contract22.auto_renew_period, contract22.auto_renew_period_units
      )

    val lastInsertId = SimpleFunction.nullary[Long]("LAST_INSERT_ID")
    return (for (c <- Contract22 if c.id === lastInsertId) yield c).list.head.id.get
  }

  def update(id: Long, contract22: Contract22) = withSession{
    val q = for { c <- Contract22 if c.id===id} yield c
    val newValues = Contract22(Some(id), contract22.vendor_id, contract22.vendor_contract_id, contract22.billing_account, contract22.is_msa,
            contract22.msa_id, contract22.extra_info, contract22.contract_type_id, contract22.a_end_id,
            contract22.z_end_id, contract22.mrc, contract22.nrc, contract22.currency_id, contract22.budget_id,
            contract22.start_date, contract22.term, contract22.term_units, contract22.cancellation_period, contract22.cancellation_period_units,
            contract22.cancelled_date, contract22.auto_renew_period, contract22.auto_renew_period_units)
    q.update(newValues)
  }

  def delete(id: Long) = withSession{
    val q = for { c <- Contract22 if c.id===id} yield c
    q.delete
  }

  def options(msaOnly: Boolean = false): Seq[(String, String)] = withSession{
    val q = for{
      (c, v) <- Contract22 innerJoin Company on (_.vendor_id === _.id)
    //                if(msaOnly && c.is_msa===1)
    } yield(c.id, v.name, c.vendor_contract_id)

    q.list.map{case (a, b, c) => (a.toString, b.toString + " " +c.toString)}
  }

//    def options(msaOnly: Boolean = false): Seq[(String, String)] = withSession{
//      val q = for{
//        (c, v) <- Contract22 innerJoin Company on (_.vendor_id === _.id)
//      //                if(msaOnly && c.is_msa===1)
//      } yield(c.id, v.name, c.vendor_contract_id, c.is_msa)
//
//      if(!msaOnly)
//        q.list.map{case (a, b, c, d) => (a.toString, b.toString + " " +c.toString)}
//      else
//        q.list.filter{case (a, b, c, d)=>d ==1}.map{case (a, b, c, d) => (a.toString, b.toString + " " +c.toString)}
//    }

  def MSAOptions: Seq[(String, String)] = options(true)
}

case class Contract4(id:Option[Long], description: Option[String], attention: Option[String], last_modifying_user: Option[String], last_modified_time: Option[Time])

object Contract4 extends Table[Contract4]("contract") with DbUtils{
  def id = column[Long]("id", O.PrimaryKey)
  def description = column[String]("description")
  def attention = column[String]("attention")
  def last_modifying_user = column[String]("last_modifying_user")
  def last_modified_time = column[Time]("last_modified_time")

  def * = id.? ~ description.? ~ attention.? ~ last_modifying_user.? ~ last_modified_time.? <> (Contract4.apply _, Contract4.unapply _)

  def all(): List[Contract4] = withSession {
    Query(Contract4) list
  }

  def filtered(filter: ContractFilter): List[Contract4] = withSession {
    this.all()
    //		DB.withConnection { implicit connection =>
    //      Logger.debug("My SQL string: " + "select * from contract " + filter.sqlCondition)
    //			SQL("select * from contract " + filter.sqlCondition).as(contract *)
    //		}// and do something here to let the contract filter pick which ones are kept. Have filter method on contract filter object.
  }

  def findById(id: Long): Option[Contract4] = withSession {
    val q = for{
      c4 <- Contract4 if c4.id===id
    }yield c4
    q.firstOption
  }

  def update(id: Long, contract4: Contract4) = withSession {
    val q = for { c <- Contract4 if c.id===id} yield c
    val newValues = Contract4(Some(id), contract4.description, contract4.attention, contract4.last_modifying_user, contract4.last_modified_time)
    q.update(newValues)
  }

  def delete(id: Long) = withSession{
    val q = for { c <- Contract4 if c.id===id} yield c
    q.delete
  }
}