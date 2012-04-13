package models

import java.io.File
import play.api.Play
import play.api.Play.current

case class Attachment(fileName: String, contractId: String) {
	def fullPath(): String = { Attachment.attachmentPath(contractId, fileName) }
}

object Attachment {

	// This must end with a slash
	val attachmentDirectory = Play.configuration.getString("attachment.path").getOrElse("/var/contracto") + "/"

	// This does not end with a slash
	def contractDirectoryPath(contractId: String): String = { attachmentDirectory + contractId }

	// This does not end with a slash
	def attachmentPath(contractId: String, attachmentName: String): String = {
		Attachment.contractDirectoryPath(contractId) + "/" + attachmentName
	}

	def contractDirectory(contractId: String): File = { new File(contractDirectoryPath(contractId)) }

	def getContractAttachmentNames(contractId: String): Array[String] = {
		return new File(attachmentDirectory + contractId).list
	}

	def getContractAttachments(contractId: String): List[Attachment] = {
		var attachments = List[Attachment]()
		val attachmentNames = getContractAttachmentNames(contractId)
		if (attachmentNames != null) {
			attachmentNames.foreach { name =>
				{
					//println("Attachment name is " + name)
					attachments ::= Attachment(name, contractId)
				}
			}
		}
		//println(attachments)
		return attachments.sortBy(_.fileName)
	}

}
