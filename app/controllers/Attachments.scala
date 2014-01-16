package controllers

// File uploader is http://valums.com/ajax-upload

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import java.io.File
import java.io.IOException

import models.Attachment
import models.Contract
import models.Company
import FileHelper._

object Attachments extends Controller {

	/**
		@returns True if it could change the contract id, false if it could not
		*/
	def changeVendorContractId(companyName: String, oldVendorContractId: String, newVendorContractId: String): Boolean = {
		if (oldVendorContractId != newVendorContractId) {

			val oldPath = Attachment.contractDirPath(companyName, oldVendorContractId)
			val newPath = Attachment.contractDirPath(companyName, newVendorContractId)
			// Complain if something is already at the new path.
			val newDir = new File(newPath)
			if (newDir.exists()) return false;

			// Not a problem if old path doesn't exist. There may just be no attachments. Simply return.
			val oldDir = new File(oldPath)
			if (!oldDir.exists()) return true;

			return oldDir.renameTo(newDir)
			
		} else return true
	}

	def changeCompanyName(currentCompanyName: String, newCompanyName: String): Boolean = {
		if (currentCompanyName != newCompanyName) {

			val newDir = Attachment.companyDir(newCompanyName)
			// Complain if something is already at the new path.
			if (newDir.exists()) return false;

			// Not a problem if old path doesn't exist. There may just be no attachments. Simply return.
			val oldDir = Attachment.companyDir(currentCompanyName)
			if (!oldDir.exists()) return true;

			return oldDir.renameTo(newDir)
			
		} else return true
	}
  
	def upload(companyId: Long, vendorContractId: String) = Action(parse.temporaryFile) { implicit request =>
		val fileName = request.queryString("qqfile").head
		// TODO handle missing company
		val company = Company.findById(companyId).get
		try {
			Logger.debug("Moving new attachment to " + Attachment.attachmentPath(company.name, vendorContractId, fileName))
			request.body.moveTo(new File(Attachment.attachmentPath(company.name, vendorContractId, fileName)))
			Ok("{\"success\": true}")
		} catch {
			case e:IOException => {
				Ok("{\"error\": \"" + e.getMessage + "\"}")
			}
		}
	}

	/** View a contract attachment */
	def view(companyName: String, vendorContractId: String, fileName: String) = Action { implicit request =>
			Ok.sendFile(
				content = new java.io.File(Attachment.attachmentPath(companyName, vendorContractId, fileName)),
				inline = true,
				fileName = _ => fileName
			)
		/*}
		else
			//TODO handle error better. trying to view attachment, but company isn't defined
			Ok("error: company with id " + companyId + " isn't defined")*/
	}

	/**
		@param vendorContractId Delete the attachments for this contract.
		@return None if everything went fine, or an error message.
	*/
	def deleteAll(companyId: Long, vendorContractId: String): Option[String] = {
		val company= Company.findById(companyId)
		if (company!= None) {
			val cn = company.get.name
			Logger.debug("Deleting all attachments for " + cn + " "  + vendorContractId)
			try {
				Attachment.contractDir(cn, vendorContractId).deleteAll
				Logger.debug("Deletion worked ok")
				return None
			} catch {
				case e: Throwable => return Some(e.getMessage())
			}
		} else 
			//TODO handle error better. trying to view attachment, but company isn't defined
			None
	}

	/** Delete an attachment */
	def delete(contractId: Long, companyName: String, vendorContractId: String, name: String, mode: String) = Action { implicit request =>	
			Attachment.findAttachment(companyName, vendorContractId, name).delete
			//TODO handle error conditions

			// Now go to some reasonable destination page.
			val maybeContract = Contract.findById(contractId)
			maybeContract.map(contract => {
				mode match {
					case "view" => Redirect(routes.Contracts.view(contract.id.get))
					case "edit" => Redirect(routes.Contracts.edit(contract.id.get))
					case _ => Redirect(routes.Contracts.filtered)
				}
			}).getOrElse(Redirect(routes.Contracts.filtered))

	}
}
