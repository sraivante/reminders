package com.reminders.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Core entity representing a scheduled reminder. Contains the reminder details,
 * recurrence cycle, notification recipients (email and/or WhatsApp), and
 * ownership/active state.
 */
@Entity
@Table(name = "reminder")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReminderCycle cycle;

    @Column(name = "custom_cycle_days")
    private Integer customCycleDays;

    @Column(nullable = false, name = "reminder_date")
    private LocalDateTime reminderDate;

    @Column(length = 120)
    private String email;

    @Column(name = "owner_email", nullable = false, length = 120)
    private String ownerEmail;

    @Column(name = "whatsapp_number", length = 30)
    private String whatsappNumber;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "silenced_until")
    private LocalDateTime silencedUntil;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ReminderCycle getCycle() {
        return cycle;
    }

    public void setCycle(ReminderCycle cycle) {
        this.cycle = cycle;
    }

    public Integer getCustomCycleDays() {
        return customCycleDays;
    }

    public void setCustomCycleDays(Integer customCycleDays) {
        this.customCycleDays = customCycleDays;
    }

    public LocalDateTime getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(LocalDateTime reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public void setWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getSilencedUntil() {
        return silencedUntil;
    }

    public void setSilencedUntil(LocalDateTime silencedUntil) {
        this.silencedUntil = silencedUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
