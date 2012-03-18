package models

case class Term(id: Long, length: Int, units: TermUnits)

object Term {
	  
	def all(): List[Term] = Nil
			  
	def create(length: Int, units: TermUnits) {}
					  
	def delete(id: Long) {}
							  
}
