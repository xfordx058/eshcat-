package com.walangkaninbossing.eshcat.util

import android.content.Context
import com.walangkaninbossing.eshcat.BuildConfig
import com.walangkaninbossing.eshcat.R
import com.walangkaninbossing.eshcat.core.Statuses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.activation.DataHandler
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

object EmailService {

    private const val HOST = "smtp.gmail.com"
    private const val PORT = "465"
    private val username: String get() = BuildConfig.SMTP_USERNAME
    private val password: String get() = BuildConfig.SMTP_PASSWORD
    private const val SENDER_NAME = "eSHCAT Portal"
    private const val LOGO_CONTENT_ID = "eshcat-logo"

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun sendEmail(
        toEmail: String,
        subject: String,
        bodyText: String
    ): Result<Unit> = sendHtmlEmail(
        toEmail = toEmail,
        subject = subject,
        htmlContent = "<p style=\"font-family: sans-serif; font-size: 14px;\">${bodyText.replace("\n", "<br>")}</p>"
    )

    suspend fun sendHtmlEmail(
        toEmail: String,
        subject: String,
        htmlContent: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (toEmail.isBlank() || !toEmail.contains("@")) {
                return@withContext Result.failure(IllegalArgumentException("Invalid target email address: $toEmail"))
            }
            if (username.isBlank() || password.isBlank()) {
                return@withContext Result.failure(IllegalStateException("Email delivery is not configured."))
            }

            val props = Properties().apply {
                put("mail.smtp.host", HOST)
                put("mail.smtp.socketFactory.port", PORT)
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.auth", "true")
                put("mail.smtp.port", PORT)
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.connectiontimeout", "10000")
                put("mail.smtp.timeout", "10000")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username, SENDER_NAME))
                addRecipient(Message.RecipientType.TO, InternetAddress(toEmail.trim()))
                setSubject(subject, "UTF-8")
                if (htmlContent.contains("cid:$LOGO_CONTENT_ID") && appContext != null) {
                    val htmlPart = MimeBodyPart().apply {
                        setContent(htmlContent, "text/html; charset=utf-8")
                    }
                    val logoPart = MimeBodyPart().apply {
                        val logoBytes = appContext!!.resources.openRawResource(R.drawable.eshcat_logo).use { it.readBytes() }
                        dataHandler = DataHandler(ByteArrayDataSource(logoBytes, "image/png"))
                        fileName = "eshcat-logo.png"
                        setHeader("Content-ID", "<$LOGO_CONTENT_ID>")
                        disposition = MimeBodyPart.INLINE
                    }
                    setContent(MimeMultipart("related").apply {
                        addBodyPart(htmlPart)
                        addBodyPart(logoPart)
                    })
                } else {
                    setContent(htmlContent, "text/html; charset=utf-8")
                }
            }

            Transport.send(message)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun buildApplicationReceivedHtml(
        fullName: String,
        referenceNumber: String,
        serviceName: String,
        status: String,
        submittedDate: String
    ): String {
        val firstName = fullName.trim().substringBefore(" ").ifBlank { "Citizen" }.escapeHtml()
        val safeServiceName = serviceName.escapeHtml()
        val safeReferenceNumber = referenceNumber.escapeHtml()
        val statusLabel = Statuses.label(status).escapeHtml()
        val safeSubmittedDate = TimeUtil.displayDate(submittedDate).escapeHtml()
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; color: #0f172a; }
                    .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; overflow: hidden; border: 1px solid #e2e8f0; }
                    .header { background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%); color: #ffffff; padding: 22px 24px; text-align: center; }
                    .logo { display: block; width: 72px; height: 72px; object-fit: contain; margin: 0 auto 12px; }
                    .header h1 { margin: 0; font-size: 22px; font-weight: 700; }
                    .header p { margin: 4px 0 0 0; font-size: 12px; opacity: 0.9; text-transform: uppercase; letter-spacing: 1px; }
                    .content { padding: 28px 24px; }
                    .greeting { font-size: 18px; font-weight: 700; margin-bottom: 12px; color: #0f172a; }
                    .message { font-size: 14px; line-height: 1.6; color: #475569; margin-bottom: 20px; }
                    .ref-box { background: #eff6ff; border: 2px dashed #3b82f6; border-radius: 12px; padding: 18px; text-align: center; margin: 20px 0; }
                    .ref-label { font-size: 12px; text-transform: uppercase; color: #1e40af; font-weight: 700; letter-spacing: 1px; margin-bottom: 4px; }
                    .ref-code { font-family: monospace; font-size: 24px; font-weight: 800; color: #1d4ed8; letter-spacing: 2px; }
                    .details-table { width: 100%; border-collapse: collapse; margin-top: 16px; background: #f8fafc; border-radius: 8px; }
                    .details-table td { padding: 12px 16px; font-size: 13px; border-bottom: 1px solid #e2e8f0; }
                    .details-table tr:last-child td { border-bottom: none; }
                    .label-col { font-weight: 600; color: #64748b; width: 35%; }
                    .value-col { font-weight: 600; color: #0f172a; }
                    .status-badge { display: inline-block; padding: 4px 12px; background: #dcfce7; color: #15803d; border-radius: 999px; font-weight: 700; font-size: 12px; }
                    .footer { background: #f1f5f9; padding: 16px 24px; text-align: center; font-size: 12px; color: #64748b; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <img class="logo" src="cid:$LOGO_CONTENT_ID" alt="eSHCAT logo" />
                        <h1>eSHCAT Municipal Portal</h1>
                        <p>Local Government Unit of Catarman</p>
                    </div>
                    <div class="content">
                        <div class="greeting">Hello, $firstName,</div>
                        <div class="message">Thank you for using eSHCAT. We have received your application for <strong>$safeServiceName</strong> and forwarded it to the appropriate office for processing.</div>
                        
                        <div class="ref-box">
                            <div class="ref-label">Official Reference Number</div>
                            <div class="ref-code">$safeReferenceNumber</div>
                        </div>

                        <table class="details-table">
                            <tr>
                                <td class="label-col">Service Requested</td>
                                <td class="value-col">$safeServiceName</td>
                            </tr>
                            <tr>
                                <td class="label-col">Current Status</td>
                                <td class="value-col"><span class="status-badge">$statusLabel</span></td>
                            </tr>
                            <tr>
                                <td class="label-col">Date Submitted</td>
                                <td class="value-col">$safeSubmittedDate</td>
                            </tr>
                        </table>
                    </div>
                    <div class="footer">
                        <p><strong>Catarman Municipal Government Services Portal</strong></p>
                        <p>Please keep your reference number and use it to track your application in the eSHCAT app.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun buildStatusUpdateHtml(
        fullName: String,
        referenceNumber: String,
        serviceName: String,
        newStatus: String,
        notes: String?
    ): String {
        val firstName = fullName.trim().substringBefore(" ").ifBlank { "Citizen" }.escapeHtml()
        val safeServiceName = serviceName.escapeHtml()
        val safeReferenceNumber = referenceNumber.escapeHtml()
        val statusLabel = Statuses.label(newStatus).escapeHtml()
        val updateMessage = when (newStatus) {
            Statuses.READY -> "Your document is ready for release. Please visit the designated municipal office during its regular business hours to claim it. Bring a valid government-issued ID and your reference number."
            Statuses.APPROVED -> "Your application has been approved and will proceed to the next stage of processing."
            Statuses.ADDITIONAL_REQUIREMENTS -> "Additional requirements are needed before we can continue processing your application. Please review the staff note below and submit the requested documents."
            Statuses.REJECTED -> "Your application was not approved. Please review the staff note below for the reason or further guidance."
            Statuses.COMPLETED -> "Your application has been completed. Thank you for using eSHCAT."
            else -> "Your application has been updated by the responsible municipal office."
        }
        val notesHtml = if (!notes.isNullOrBlank()) {
            "<tr><td class=\"label-col\">Staff Note</td><td class=\"value-col\">${notes.escapeHtml()}</td></tr>"
        } else ""

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; color: #0f172a; }
                    .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; overflow: hidden; border: 1px solid #e2e8f0; }
                    .header { background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%); color: #ffffff; padding: 22px 24px; text-align: center; }
                    .logo { display: block; width: 72px; height: 72px; object-fit: contain; margin: 0 auto 12px; }
                    .header h1 { margin: 0; font-size: 22px; font-weight: 700; }
                    .header p { margin: 4px 0 0 0; font-size: 12px; opacity: 0.9; text-transform: uppercase; letter-spacing: 1px; }
                    .content { padding: 28px 24px; }
                    .greeting { font-size: 18px; font-weight: 700; margin-bottom: 12px; color: #0f172a; }
                    .message { font-size: 14px; line-height: 1.6; color: #475569; margin-bottom: 20px; }
                    .ref-box { background: #eff6ff; border: 2px dashed #3b82f6; border-radius: 12px; padding: 18px; text-align: center; margin: 20px 0; }
                    .ref-label { font-size: 12px; text-transform: uppercase; color: #1e40af; font-weight: 700; letter-spacing: 1px; margin-bottom: 4px; }
                    .ref-code { font-family: monospace; font-size: 24px; font-weight: 800; color: #1d4ed8; letter-spacing: 2px; }
                    .details-table { width: 100%; border-collapse: collapse; margin-top: 16px; background: #f8fafc; border-radius: 8px; }
                    .details-table td { padding: 12px 16px; font-size: 13px; border-bottom: 1px solid #e2e8f0; }
                    .details-table tr:last-child td { border-bottom: none; }
                    .label-col { font-weight: 600; color: #64748b; width: 35%; }
                    .value-col { font-weight: 600; color: #0f172a; }
                    .status-badge { display: inline-block; padding: 4px 12px; background: #dbeafe; color: #1e40af; border-radius: 999px; font-weight: 700; font-size: 12px; }
                    .footer { background: #f1f5f9; padding: 16px 24px; text-align: center; font-size: 12px; color: #64748b; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <img class="logo" src="cid:$LOGO_CONTENT_ID" alt="eSHCAT logo" />
                        <h1>eSHCAT Municipal Portal</h1>
                        <p>Local Government Unit of Catarman</p>
                    </div>
                    <div class="content">
                        <div class="greeting">Hello, $firstName,</div>
                        <div class="message">$updateMessage Your application is for <strong>$safeServiceName</strong>.</div>
                        
                        <div class="ref-box">
                            <div class="ref-label">Reference Number</div>
                            <div class="ref-code">$safeReferenceNumber</div>
                        </div>

                        <table class="details-table">
                            <tr>
                                <td class="label-col">Service</td>
                                <td class="value-col">$safeServiceName</td>
                            </tr>
                            <tr>
                                <td class="label-col">New Status</td>
                                <td class="value-col"><span class="status-badge">$statusLabel</span></td>
                            </tr>
                            $notesHtml
                        </table>
                    </div>
                    <div class="footer">
                        <p><strong>Catarman Municipal Government Services Portal</strong></p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun String.escapeHtml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
}
