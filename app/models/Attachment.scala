package models

import java.io.File

case class Attachment(fileName: String, contractId: String) {
	def fullPath(): String = { Attachment.attachmentPath(contractId, fileName) }
}

object Attachment {

	// This must end with a slash
	val attachmentDirectory = "/tmp/Attachments/"

	// This does not end with a slash
	def contractDirectoryPath(contractId: String): String = { attachmentDirectory + contractId }

	// This does not end with a slash
	def attachmentPath(contractId: String, attachmentName: String): String = {
		Attachment.contractDirectoryPath(contractId) + "/" + attachmentName
	}

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
		return attachments
	}

}
