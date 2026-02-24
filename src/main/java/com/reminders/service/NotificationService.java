package com.reminders.service;

import com.reminders.model.NotificationChannel;
import com.reminders.model.NotificationLog;
import com.reminders.model.Reminder;
import com.reminders.model.ReminderCycle;
import com.reminders.repository.NotificationLogRepository;
import com.reminders.repository.ReminderRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static com.reminders.RemindersApplication.ANSI_BOLD_YELLOW;
import static com.reminders.RemindersApplication.ANSI_RESET;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationLogRepository notificationLogRepository;
    private final ReminderRepository reminderRepository;
    private final EmailNotificationSender emailNotificationSender;
    private final WhatsAppNotificationSender whatsAppNotificationSender;

    public NotificationService(
            NotificationLogRepository notificationLogRepository,
            ReminderRepository reminderRepository,
            EmailNotificationSender emailNotificationSender,
            WhatsAppNotificationSender whatsAppNotificationSender
    ) {
        this.notificationLogRepository = notificationLogRepository;
        this.reminderRepository = reminderRepository;
        this.emailNotificationSender = emailNotificationSender;
        this.whatsAppNotificationSender = whatsAppNotificationSender;
    }

    @Transactional
    public void sendNotifications(Reminder reminder, String messageBody, LocalDateTime now) {
        if (!reminderRepository.existsById(reminder.getId())) {
            return;
        }
        String slotKey = slotKey(reminder.getCycle(), now);
        if (StringUtils.hasText(reminder.getEmail()) && canSend(reminder.getId(), NotificationChannel.EMAIL, slotKey)) {
            log.info(ANSI_BOLD_YELLOW+"[NOTIFY][EMAIL][START] reminderId={} recipient={} slot={}"+ANSI_RESET,
                    reminder.getId(), reminder.getEmail(), slotKey);
            boolean sent = emailNotificationSender.send(reminder, messageBody);
            if (sent) {
                log(reminder.getId(), NotificationChannel.EMAIL, slotKey);
                log.info(ANSI_BOLD_YELLOW+"[NOTIFY][EMAIL][SUCCESS] reminderId={} recipient={} slot={}"+ANSI_RESET,
                        reminder.getId(), reminder.getEmail(), slotKey);
            } else {
                log.warn("[NOTIFY][EMAIL][FAILED] reminderId={} recipient={} slot={}",
                        reminder.getId(), reminder.getEmail(), slotKey);
            }
        } else if (StringUtils.hasText(reminder.getEmail())) {
            log.info("[NOTIFY][EMAIL][SKIPPED] reminderId={} recipient={} slot={} reason=already-sent",
                    reminder.getId(), reminder.getEmail(), slotKey);
        }

        if (StringUtils.hasText(reminder.getWhatsappNumber()) && canSend(reminder.getId(), NotificationChannel.WHATSAPP, slotKey)) {
            log.info("[NOTIFY][WHATSAPP][START] reminderId={} recipient={} slot={}",
                    reminder.getId(), reminder.getWhatsappNumber(), slotKey);
            boolean sent = whatsAppNotificationSender.send(reminder, messageBody);
            if (sent) {
                log(reminder.getId(), NotificationChannel.WHATSAPP, slotKey);
                log.info("[NOTIFY][WHATSAPP][SUCCESS] reminderId={} recipient={} slot={}",
                        reminder.getId(), reminder.getWhatsappNumber(), slotKey);
            } else {
                log.warn("[NOTIFY][WHATSAPP][FAILED] reminderId={} recipient={} slot={}",
                        reminder.getId(), reminder.getWhatsappNumber(), slotKey);
            }
        } else if (StringUtils.hasText(reminder.getWhatsappNumber())) {
            log.info("[NOTIFY][WHATSAPP][SKIPPED] reminderId={} recipient={} slot={} reason=already-sent",
                    reminder.getId(), reminder.getWhatsappNumber(), slotKey);
        }
    }

    private boolean canSend(Long reminderId, NotificationChannel channel, String slotKey) {
        return !notificationLogRepository.existsByReminderIdAndChannelAndSlotKey(reminderId, channel, slotKey);
    }

    private void log(Long reminderId, NotificationChannel channel, String slotKey) {
        try {
            notificationLogRepository.save(new NotificationLog(reminderId, channel, slotKey, LocalDateTime.now()));
        } catch (DataIntegrityViolationException ex) {
            log.warn("Skipping notification log insert for deleted/invalid reminder {}", reminderId);
        }
    }

    private String slotKey(ReminderCycle cycle, LocalDateTime now) {
        if (cycle == ReminderCycle.MINUTELY || cycle == ReminderCycle.HOURLY) {
            LocalDateTime minuteSlot = now.truncatedTo(ChronoUnit.MINUTES);
            return minuteSlot.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        if (cycle == ReminderCycle.DAILY) {
            LocalDateTime hourSlot = now.truncatedTo(ChronoUnit.HOURS);
            return hourSlot.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"));
        }
        return now.toLocalDate().toString();
    }
}
