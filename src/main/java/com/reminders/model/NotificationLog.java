package com.reminders.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification_log",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notification_slot", columnNames = {"reminder_id", "channel", "slot_key"})
        }
)
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reminder_id", nullable = false)
    private Long reminderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "slot_key", nullable = false, length = 30)
    private String slotKey;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public NotificationLog() {
    }

    public NotificationLog(Long reminderId, NotificationChannel channel, String slotKey, LocalDateTime sentAt) {
        this.reminderId = reminderId;
        this.channel = channel;
        this.slotKey = slotKey;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public Long getReminderId() {
        return reminderId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getSlotKey() {
        return slotKey;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
