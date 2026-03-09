package com.reminders.repository;

import com.reminders.model.NotificationChannel;
import com.reminders.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link NotificationLog} entities.
 * Provides duplicate-detection and cleanup queries for notification records.
 */
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    boolean existsByReminderIdAndChannelAndSlotKey(Long reminderId, NotificationChannel channel, String slotKey);

    long deleteByReminderId(Long reminderId);
}
