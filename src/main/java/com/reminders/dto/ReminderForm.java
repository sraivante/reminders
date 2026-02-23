package com.reminders.dto;

import com.reminders.model.ReminderCycle;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class ReminderForm {

    private Long id;

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotNull
    private ReminderCycle cycle;

    @Min(1)
    private Integer customCycleDays;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime reminderDate;

    @Email
    @Size(max = 120)
    private String email;

    @Size(max = 30)
    private String whatsappNumber;

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

    @AssertTrue(message = "Custom cycle requires custom days > 0")
    public boolean isCustomCycleValid() {
        return cycle != ReminderCycle.CUSTOM || (customCycleDays != null && customCycleDays > 0);
    }
}
