package model

case class ContractCategory(id: Long, label: String)

object ContractCategory {
	  
	def all(): List[ContractCategory] = Nil
			  
	def create(label: String) {}
					  
	def delete(id: Long) {}
							  
}
