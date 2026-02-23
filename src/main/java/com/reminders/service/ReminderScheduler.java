package com.reminders.service;

import com.reminders.model.Reminder;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);
    private final ReminderService reminderService;
    private final NotificationService notificationService;

    public ReminderScheduler(ReminderService reminderService, NotificationService notificationService) {
        this.reminderService = reminderService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "${app.notification.cron:0 * * * * *}")
    public void runReminderNotifications() {
        LocalDateTime now = LocalDateTime.now();

        List<Reminder> reminders = reminderService.findNotificationCandidates();
        for (Reminder reminder : reminders) {
            try {
                if (!reminderService.shouldNotifyNow(reminder, now)) {
                    continue;
                }
                String message = reminderService.buildMessage(reminder);
                notificationService.sendNotifications(reminder, message, now);
            } catch (Exception ex) {
                log.error("Failed processing reminder {}", reminder.getId(), ex);
            }
        }
    }
}
