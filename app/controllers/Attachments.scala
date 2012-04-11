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
  
	def upload(contractId: String) = Action(parse.temporaryFile) { implicit request =>
		println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
		//val contractId = request.queryString("contractId").head
		val fileName = request.queryString("qqfile").head
		//println(request)
		//println("id: " + contractId)
		//println("file name: " + fileName)
		try {
			request.body.moveTo(new File("/tmp/" + contractId + "/" + fileName))
			//println("Replying ok")
			Ok("{\"success\": true}")
		} catch {
			case e:IOException => {
				//println("Got io exception. " + e.getMessage);
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
