package com.freightflow.modules.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Transactional e-mail service for FreightFlow operational notifications.
 *
 * <p>This bean is only created when {@code spring.mail.username} is set.
 * When the property is absent (local dev or Railway without MAIL_USERNAME),
 * Spring does not instantiate this class and callers that inject it with
 * {@code @Autowired(required = false)} receive {@code null} — they must
 * null-check before calling any method.</p>
 *
 * <h3>Templates</h3>
 * <p>Templates are defined inline as Java strings (no Thymeleaf dependency).
 * Each method builds a minimal HTML e-mail using table-based layout compatible
 * with the major e-mail clients.</p>
 */
@Service
@ConditionalOnProperty(prefix = "spring.mail", name = "username", matchIfMissing = false)
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${freightflow.notification.from-email:noreply@freightflow.com}")
    private String fromEmail;

    @Value("${freightflow.notification.from-name:FreightFlow}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Sends a critical-alert notification to the customer contact.
     *
     * @param to              recipient e-mail address
     * @param shipmentBooking booking number for context
     * @param alertType       alert type (e.g. DELAY, CUSTOMS_HOLD)
     * @param message         human-readable alert message
     */
    public void sendAlertNotification(String to, String shipmentBooking,
                                      String alertType, String message) {
        String subject = "[FreightFlow] Alert on booking " + shipmentBooking + " — " + alertType;
        String body = alertHtml(shipmentBooking, alertType, message);
        send(to, subject, body);
    }

    /**
     * Notifies a customer when their shipment reaches a terminal status.
     *
     * @param to              recipient e-mail address
     * @param shipmentBooking booking number for context
     * @param newStatus       the new shipment status (e.g. ARRIVED, DELIVERED)
     * @param eta             estimated or actual arrival time as a formatted string
     */
    public void sendStatusChangeNotification(String to, String shipmentBooking,
                                             String newStatus, String eta) {
        String subject = "[FreightFlow] Shipment " + shipmentBooking + " is now " + newStatus;
        String body = statusChangeHtml(shipmentBooking, newStatus, eta);
        send(to, subject, body);
    }

    /**
     * Notifies a customer that a new document is available for download.
     *
     * @param to              recipient e-mail address
     * @param shipmentBooking booking number for context
     * @param documentType    type of document (CTE, BL, NF, OTHER)
     * @param downloadUrl     pre-signed download URL (1-hour TTL)
     */
    public void sendDocumentAvailable(String to, String shipmentBooking,
                                      String documentType, String downloadUrl) {
        String subject = "[FreightFlow] New document available — " + documentType + " for " + shipmentBooking;
        String body = documentAvailableHtml(shipmentBooking, documentType, downloadUrl);
        send(to, subject, body);
    }

    // ── Send helper ──────────────────────────────────────────────────────────

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML
            mailSender.send(message);
            log.info("Email sent: to={} subject={}", to, subject);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to={} subject={}: {}", to, subject, e.getMessage());
        }
    }

    // ── HTML templates ───────────────────────────────────────────────────────

    private String alertHtml(String booking, String alertType, String message) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <body style="font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px">
                  <table width="600" cellpadding="0" cellspacing="0"
                         style="background:#fff;border-radius:8px;overflow:hidden;margin:auto">
                    <tr>
                      <td style="background:#1e3a5f;padding:24px 32px">
                        <span style="color:#fff;font-size:20px;font-weight:bold">FreightFlow</span>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:32px">
                        <h2 style="color:#c0392b;margin:0 0 16px">🚨 Alert: %s</h2>
                        <p style="color:#555;margin:0 0 8px">
                          <strong>Booking:</strong> %s
                        </p>
                        <p style="color:#555;margin:0 0 24px">%s</p>
                        <p style="color:#888;font-size:12px">
                          Log in to <a href="https://app.freightflow.com">FreightFlow</a>
                          to view full details and take action.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#f4f4f4;padding:16px 32px;color:#aaa;font-size:11px;text-align:center">
                        © FreightFlow — automated notification, do not reply
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(alertType, booking, message);
    }

    private String statusChangeHtml(String booking, String status, String eta) {
        String color = "DELIVERED".equals(status) ? "#27ae60" : "#2980b9";
        String icon  = "DELIVERED".equals(status) ? "✅" : "🚢";
        String etaRow = (eta != null && !eta.isBlank())
                ? "<p style=\"color:#555;margin:0 0 8px\"><strong>ETA / ATA:</strong> " + eta + "</p>"
                : "";
        return """
                <!DOCTYPE html>
                <html lang="en">
                <body style="font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px">
                  <table width="600" cellpadding="0" cellspacing="0"
                         style="background:#fff;border-radius:8px;overflow:hidden;margin:auto">
                    <tr>
                      <td style="background:#1e3a5f;padding:24px 32px">
                        <span style="color:#fff;font-size:20px;font-weight:bold">FreightFlow</span>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:32px">
                        <h2 style="color:%s;margin:0 0 16px">%s Shipment Status Update</h2>
                        <p style="color:#555;margin:0 0 8px">
                          <strong>Booking:</strong> %s
                        </p>
                        <p style="color:#555;margin:0 0 8px">
                          <strong>New Status:</strong> %s
                        </p>
                        %s
                        <p style="color:#888;font-size:12px;margin-top:24px">
                          Track your shipment at
                          <a href="https://app.freightflow.com">FreightFlow</a>.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#f4f4f4;padding:16px 32px;color:#aaa;font-size:11px;text-align:center">
                        © FreightFlow — automated notification, do not reply
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(color, icon, booking, status, etaRow);
    }

    private String documentAvailableHtml(String booking, String docType, String downloadUrl) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <body style="font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px">
                  <table width="600" cellpadding="0" cellspacing="0"
                         style="background:#fff;border-radius:8px;overflow:hidden;margin:auto">
                    <tr>
                      <td style="background:#1e3a5f;padding:24px 32px">
                        <span style="color:#fff;font-size:20px;font-weight:bold">FreightFlow</span>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:32px">
                        <h2 style="color:#1e3a5f;margin:0 0 16px">📄 New Document Available</h2>
                        <p style="color:#555;margin:0 0 8px">
                          <strong>Booking:</strong> %s
                        </p>
                        <p style="color:#555;margin:0 0 24px">
                          <strong>Document type:</strong> %s
                        </p>
                        <a href="%s"
                           style="background:#1e3a5f;color:#fff;padding:12px 24px;
                                  text-decoration:none;border-radius:4px;font-weight:bold">
                          Download Document
                        </a>
                        <p style="color:#888;font-size:12px;margin-top:24px">
                          This link expires in 1 hour. If expired, log in to
                          <a href="https://app.freightflow.com">FreightFlow</a>
                          to generate a new one.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#f4f4f4;padding:16px 32px;color:#aaa;font-size:11px;text-align:center">
                        © FreightFlow — automated notification, do not reply
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(booking, docType, downloadUrl);
    }
}
