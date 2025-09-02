package com.dataguard.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * A utility class for sending emails.
 *
 * Technical Decision:
 * - This class uses the JavaMail API, which is the standard way to send emails in Java/Kotlin.
 * - The `sendEmail` function is a `suspend` function running on `Dispatchers.IO`. This is critical
 *   because networking operations are long-running and cannot be performed on the main UI thread.
 *   Using a coroutine with the IO dispatcher ensures that email sending is done in the background
 *   without freezing the app.
 * - Security Note: Storing and using email credentials directly in the app is a security risk.
 *   The `README.md` and in-app instructions strongly advise the user to create and use a
 *   Google "App Password" instead of their main account password to mitigate this risk.
 */
class EmailSender {

    suspend fun sendEmail(
        senderEmail: String,
        senderPassword: String, // Should be a Google App Password
        recipientEmail: String,
        deviceInfo: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                val props = Properties().apply {
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.socketFactory.port", "465")
                    put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.port", "465")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(senderEmail, senderPassword)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(senderEmail))
                    addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                    subject = "Data Guard - Data Usage Limit Exceeded"
                    setText(
                        "Hello,\n\nThis is an automated alert from Data Guard.\n\n" +
                                "The device '$deviceInfo' has exceeded its configured data usage limit.\n\n" +
                                "Please check your usage to avoid unexpected charges.\n\n" +
                                "Thank you,\nData Guard"
                    )
                }

                Transport.send(message)
                Log.d("EmailSender", "Email sent successfully.")

            } catch (e: Exception) {
                Log.e("EmailSender", "Failed to send email", e)
            }
        }
    }
}
