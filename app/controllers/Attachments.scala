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

object Attachments extends Controller {

	/**
		@returns True if it could change the contract id, false if it could not
		*/
	def changeContractId(oldContractId: String, newContractId: String): Boolean = {
		if (oldContractId != newContractId) {

			val oldPath = Attachment.contractDirectoryPath(oldContractId)
			val newPath = Attachment.contractDirectoryPath(newContractId)
			// Complain if something is already at the new path.
			val newDir = new File(newPath)
			if (newDir.exists()) return false;

			// Not a problem if old path doesn't exist. There may just be no attachments. Simply return.
			val oldDir = new File(oldPath)
			if (!oldDir.exists()) return true;

			return oldDir.renameTo(newDir)
			
		} else return true
	}
  
	def upload(contractId: String) = Action(parse.temporaryFile) { implicit request =>
		val fileName = request.queryString("qqfile").head
		try {
			request.body.moveTo(new File(Attachment.attachmentPath(contractId, fileName)))
			Ok("{\"success\": true}")
		} catch {
			case e:IOException => {
				Ok("{\"error\": \"" + e.getMessage + "\"}")
			}
		}
	}

	/** View a contract attachment */
	def view(contractId: String, name: String) = Action { implicit request =>
		Ok.sendFile(
			content = new java.io.File(Attachment.attachmentPath(contractId, name)),
			inline = true,
			fileName = _ => name
		)
	}

	def delete(contractId: String, name: String, mode: String) = Action { implicit request =>	
		//TODO handle error conditions
		new java.io.File(Attachment.attachmentPath(contractId, name)).delete
		val maybeContract = Contract.findByContractId(contractId)
		maybeContract.map(contract => {
			mode match {
				case "view" => Redirect(routes.Contracts.view(contract.id.get))
				//case "view" => Ok(views.html.contract.view(contract, "Deleted attachment \"" + name "\""", "ZYZZY"))
				case "edit" => Redirect(routes.Contracts.edit(contract.id.get))
				case _ => Redirect(routes.Contracts.filtered)
			}
		}).getOrElse(Redirect(routes.Contracts.filtered))
	}
}
