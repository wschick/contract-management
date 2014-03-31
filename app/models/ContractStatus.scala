package models
// http://stackoverflow.com/questions/1898932/case-classes-vs-enumerations-in-scala

sealed trait ContractStatus { def value: Int; def name: String }

case object OK extends ContractStatus { val value = 0; val name = "ok"; }
case object FARWARNING extends ContractStatus { val value = 1 ; val name = "far-warning";}
case object NEARWARNING extends ContractStatus { val value = 2 ; val name = "near-warning";}
case object TOOLATE extends ContractStatus { val value = 3 ; val name = "too-late";}
case object CANCELLED extends ContractStatus { val value = 4 ; val name = "cancelled";}
case object MONTH2MONTH extends ContractStatus { val value = 5 ; val name = "month2month";}
