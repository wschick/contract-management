package models

case class Company(id: Long, name: String, contactPerson: String, contactEmail: String, contactPhone: String)

object Company {
	  
	def all(): List[Company] = Nil
			  
  def create(name: String, contactPerson: String, contactEmail: String, contactPhone: String) {}
					  
  def delete(id: Long) {}
							  
}
