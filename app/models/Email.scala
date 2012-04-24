package models

/*import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.data._
import play.api.data.Forms._
*/
import play.api.Play
import play.api.Play.current
import javax.mail._
import javax.mail.internet._

object Email {

	val from = Play.configuration.getString("reminder.from").getOrElse("Contracto")

	def bodyText(dayString: String, contract: Contract, contractURL: String): String = {
		dayString + " left before cancelling contract " + contract.name + "\n\n" +
		"Contract " + Company.findById(contract.companyId).get.name + " " + contract.vendorContractId + "\n" +
		"Ends on " + contract.lastDay + ". Cancel by " + contract.cancellationDate + ", " +
		contract.cancellationPeriod + " before its end." + "\n\n" + 
		"View contract at " + contractURL
	}


	def sendReminder(reminder: Reminder, to: Iterable[String], contractURL: String): Option[String] = {

		val daysLeft = reminder.contract.daysUntilCancellationDate()
		val dayString = daysLeft match {
			case 1 => "1 day"
			case i: Int => i + "days"
		}
		val body = bodyText(dayString, reminder.contract, contractURL)

		println("Body is\n" + body)
		send(to, from, dayString + " to cancel " + reminder.contract.name, body)

	}
	  
	/** Send an email to one or more recipients
		@param an iterable of email addresses of recipients
		@param from email address the message appears to be from
		@param subject email subject
		@param body email body
		**/
	def send(to: Iterable[String], from: String, subject: String, body: String): Option[String] = {
		send(to.reduceLeft[String]{(list, item) => item + "," + list}, from, subject, body)
	}

	/** Send an email to one or more recipients
		@param to comma-separated list of email addresses of recipients
		@param from email address the message appears to be from
		@param subject email subject
		@param body email body
		**/
	def send(to: String, from: String, subject: String, body: String): Option[String] = {
		val session = Session.getDefaultInstance(System.getProperties)
		val message = new MimeMessage(session)
		message.setFrom(new InternetAddress(from))
		message.setRecipients(Message.RecipientType.TO, to)
		message.setSubject(subject)
		message.setText(body)

		try {
			Transport.send(message)
		} catch {
			case sfe: SendFailedException => { return Some("Send failed " + sfe) }
			case me: MessagingException => { return Some("Messaging exception " + me) }
		}
		return None
	}

}
