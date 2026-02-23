package com.reminders.service;

import com.reminders.model.Reminder;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(recipient);
            if (StringUtils.hasText(fromEmail)) {
                mail.setFrom(fromEmail);
            }
            mail.setSubject("Reminder alert: " + reminder.getTitle());
            mail.setText(messageBody);
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
}
