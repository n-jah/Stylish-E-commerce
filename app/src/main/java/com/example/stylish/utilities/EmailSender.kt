package com.example.stylish.utilities

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

suspend fun sendEmail(
    recipient: String,
    subject: String,
    messageBody: String
) {
    withContext(Dispatchers.IO) { // Perform email sending in the IO context
        try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        "zxnagah@gmail.com", // Replace with your email
                        "neqc epwm qria sdgb"    // Replace with your app-specific password
                    )
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress("zxnagah@gmail.com")) // Sender email
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient)) // Recipient email
                this.subject = subject // Email subject
                setText(messageBody) // Email body
            }

            Transport.send(message) // Send the email
            println("Email sent successfully to $recipient")
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception
            println("Failed to send email: ${e.message}")
        }
    }
}
