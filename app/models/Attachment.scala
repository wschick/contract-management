package models

import java.io.File

case class Attachment(fileName: String, contractId: String) {
	def fullPath(): String = { Attachment.attachmentPath(contractId, fileName) }
}

object Attachment {

	val attachmentDirectory = "/tmp/"

	def contractDirectoryPath(contractId: String): String = { attachmentDirectory + contractId }

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
					println("Attachment name is " + name)
					attachments ::= Attachment(name, contractId)
				}
			}
		}
		println(attachments)
		return attachments
	}

}
