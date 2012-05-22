package models

import java.io.File
import play.api.Play
import play.api.Play.current

case class Attachment(companyName: String, vendorContractId: String, fileName: String) {

	def fullPath(): String = { Attachment.attachmentPath(companyName, vendorContractId, fileName) }

	def contractDirPath(): String = Attachment.contractDirPath(companyName, vendorContractId)

}

object Attachment {

	/***** Paths *****/

	// This must end with a slash
	val attachmentDirPath = Play.configuration.getString("attachment.path").getOrElse("/var/contracto") + "/"

	// This must end with a slash
	def companyDirPath(companyName: String): String = attachmentDirPath + "/" + companyName + "/"

	// This must end with a slash
	def contractDirPath(companyName: String, vendorContractId: String): String = { 
		attachmentDirPath + companyName + "/" + vendorContractId + "/"
	}

	// This does not end with a slash
	def attachmentPath(companyName: String, vendorContractId: String, attachmentName: String): String = {
		Attachment.contractDirPath(companyName, vendorContractId) + "/" + attachmentName
	}

	/***** File Objects *****/

	def companyDir(companyName: String): File = { new File(companyDirPath(companyName)) }

	def contractDir(companyName: String, vendorContractId: String): File = { 
		new File(contractDirPath(companyName, vendorContractId)) 
	}

	def findAttachment(companyName: String, vendorContractId: String, fileName: String): File = {
		new File(attachmentPath(companyName, vendorContractId, fileName)) 
	}

	/***** Attachments *****/

	def contractHasAttachments(companyName: String, vendorContractId: String): Boolean = {
		getContractAttachmentNames(companyName, vendorContractId) != null
	}

	def getContractAttachmentNames(companyName: String, vendorContractId: String): Array[String] = {
		return contractDir(companyName, vendorContractId).list
	}

	def getContractAttachments(companyId: Long, vendorContractId: String): List[Attachment] = {
		//TODO handle error conditions better
		getContractAttachments(Company.findById(companyId).get.name, vendorContractId)
	}

	def getContractAttachments(companyName: String, vendorContractId: String): List[Attachment] = {
		var attachments = List[Attachment]()
		val attachmentNames = getContractAttachmentNames(companyName, vendorContractId)
		if (attachmentNames != null) {
			attachmentNames.foreach { name =>
				{
					//Logger.debug("Attachment name is " + name)
					attachments ::= Attachment(companyName, vendorContractId, name)
				}
			}
		}
		//Logger.debug(attachments)
		return attachments.sortBy(_.fileName)
	}

}
