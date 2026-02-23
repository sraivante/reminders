package com.reminders.service;

import com.reminders.model.NotificationChannel;
import com.reminders.model.NotificationLog;
import com.reminders.model.Reminder;
import com.reminders.model.ReminderCycle;
import com.reminders.repository.NotificationLogRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final EmailNotificationSender emailNotificationSender;
    private final WhatsAppNotificationSender whatsAppNotificationSender;

    public NotificationService(
            NotificationLogRepository notificationLogRepository,
            EmailNotificationSender emailNotificationSender,
            WhatsAppNotificationSender whatsAppNotificationSender
    ) {
        this.notificationLogRepository = notificationLogRepository;
        this.emailNotificationSender = emailNotificationSender;
        this.whatsAppNotificationSender = whatsAppNotificationSender;
    }

    @Transactional
    public void sendNotifications(Reminder reminder, String messageBody, LocalDateTime now) {
        String slotKey = slotKey(reminder.getCycle(), now);
        if (StringUtils.hasText(reminder.getEmail()) && canSend(reminder.getId(), NotificationChannel.EMAIL, slotKey)) {
            boolean sent = emailNotificationSender.send(reminder, messageBody);
            if (sent) {
                log(reminder.getId(), NotificationChannel.EMAIL, slotKey);
            }
        }

        if (StringUtils.hasText(reminder.getWhatsappNumber()) && canSend(reminder.getId(), NotificationChannel.WHATSAPP, slotKey)) {
            boolean sent = whatsAppNotificationSender.send(reminder, messageBody);
            if (sent) {
                log(reminder.getId(), NotificationChannel.WHATSAPP, slotKey);
            }
        }
    }

    private boolean canSend(Long reminderId, NotificationChannel channel, String slotKey) {
        return !notificationLogRepository.existsByReminderIdAndChannelAndSlotKey(reminderId, channel, slotKey);
    }

    private void log(Long reminderId, NotificationChannel channel, String slotKey) {
        notificationLogRepository.save(new NotificationLog(reminderId, channel, slotKey, LocalDateTime.now()));
    }

    private String slotKey(ReminderCycle cycle, LocalDateTime now) {
        if (cycle == ReminderCycle.MINUTELY) {
            LocalDateTime minuteSlot = now.truncatedTo(ChronoUnit.MINUTES);
            return minuteSlot.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        return now.toLocalDate().toString();
    }
}
