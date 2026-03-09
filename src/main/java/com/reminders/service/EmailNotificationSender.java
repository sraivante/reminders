package com.reminders.service;

import com.reminders.model.Reminder;
import java.util.concurrent.atomic.AtomicBoolean;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Sends reminder notifications via email using Spring's {@link JavaMailSender}.
 * Formats the message body as styled HTML and handles SMTP authentication failures gracefully.
 */
@Component
public class EmailNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String smtpUsername;
    private final String smtpPassword;
    private final AtomicBoolean authFailureLogged = new AtomicBoolean(false);

    public EmailNotificationSender(
            JavaMailSender mailSender,
            @Value("${app.mail.from:}") String fromEmail,
            @Value("${spring.mail.username:}") String smtpUsername,
            @Value("${spring.mail.password:}") String smtpPassword
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    /**
     * Sends an HTML-formatted email notification for the given reminder.
     *
     * @param reminder    the reminder whose recipient and title are used
     * @param messageBody plain-text message body to be converted to HTML
     * @return {@code true} if the email was sent successfully, {@code false} otherwise
     */
    public boolean send(Reminder reminder, String messageBody) {
        String recipient = reminder.getEmail();
        if (!StringUtils.hasText(recipient)) {
            return false;
        }
        if (!StringUtils.hasText(smtpUsername) || !StringUtils.hasText(smtpPassword)) {
            log.warn("Email skipped for reminder {}: SMTP username/password not configured", reminder.getId());
            return false;
        }
        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, false, "UTF-8");
            helper.setTo(recipient);
            if (StringUtils.hasText(fromEmail)) {
                helper.setFrom(fromEmail);
            }
            helper.setSubject("Reminder alert: " + reminder.getTitle());
            helper.setText(formatAsHtml(messageBody), true);
            mailSender.send(mail);
            authFailureLogged.set(false);
            return true;
        } catch (MailAuthenticationException ex) {
            if (authFailureLogged.compareAndSet(false, true)) {
                log.error(
                        "Email authentication failed. Set valid MAIL_USERNAME and Gmail App Password in MAIL_PASSWORD. "
                                + "Further auth errors will be suppressed until a successful send."
                );
            }
            return false;
        } catch (Exception ex) {
            log.error("Email send failed for reminder {}", reminder.getId(), ex);
            return false;
        }
    }

    private String formatAsHtml(String messageBody) {
        StringBuilder html = new StringBuilder("<div style='font-family:Segoe UI,Arial,sans-serif;'>");
        html.append("<p><strong style='background:#fff59d;padding:2px 6px;'>REMINDER ALERT</strong></p>");

        for (String line : messageBody.split("\\r?\\n")) {
            if (!StringUtils.hasText(line)) {
                continue;
            }
            int separator = line.indexOf(':');
            if (separator > 0 && separator < line.length() - 1) {
                String label = escapeHtml(line.substring(0, separator).trim());
                String value = escapeHtml(line.substring(separator + 1).trim());
                if ("Reminder".equalsIgnoreCase(label) || "Title".equalsIgnoreCase(label)) {
                    html.append("<p><strong>").append(label).append(":</strong> <mark>")
                            .append(value).append("</mark></p>");
                } else {
                    html.append("<p><strong>").append(label).append(":</strong> ").append(value).append("</p>");
                }
            } else {
                html.append("<p>").append(escapeHtml(line)).append("</p>");
            }
        }
        html.append("</div>");
        return html.toString();
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
