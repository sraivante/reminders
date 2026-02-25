package com.reminders.service;

import com.reminders.dto.ReminderForm;
import com.reminders.model.Reminder;
import com.reminders.model.ReminderCycle;
import com.reminders.repository.NotificationLogRepository;
import com.reminders.repository.ReminderRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final NotificationLogRepository notificationLogRepository;

    public ReminderService(ReminderRepository reminderRepository, NotificationLogRepository notificationLogRepository) {
        this.reminderRepository = reminderRepository;
        this.notificationLogRepository = notificationLogRepository;
    }

    public List<Reminder> findAllForOwner(String ownerEmail) {
        return reminderRepository.findAllByOwnerEmailOrderByReminderDateAsc(ownerEmail);
    }

    public ReminderForm findFormByIdForOwner(Long id, String ownerEmail) {
        Reminder reminder = findByIdForOwner(id, ownerEmail);
        ReminderForm form = new ReminderForm();
        form.setId(reminder.getId());
        form.setTitle(reminder.getTitle());
        form.setDescription(reminder.getDescription());
        form.setCycle(reminder.getCycle());
        form.setCustomCycleDays(reminder.getCustomCycleDays());
        form.setReminderDate(reminder.getReminderDate());
        form.setEmail(reminder.getEmail());
        form.setWhatsappNumber(reminder.getWhatsappNumber());
        return form;
    }

    public List<Reminder> findNotificationCandidates() {
        return reminderRepository.findReminderCandidates();
    }

    public boolean shouldNotifyNow(Reminder reminder, LocalDateTime now) {
        LocalDateTime due = reminder.getReminderDate();
        return switch (reminder.getCycle()) {
            case MINUTELY -> !due.isAfter(now);
            case HOURLY -> isWithinMinuteWindow(now, due, 10);
            case DAILY -> isWithinHourWindow(now, due, 5);
            case WEEKLY -> isWithinWindow(now, due, 3);
            case MONTHLY -> isWithinWindow(now, due, 5);
            case YEARLY -> isWithinWindow(now, due, 10);
            case CUSTOM -> isWithinWindow(now, due, 7);
        };
    }

    private boolean isWithinMinuteWindow(LocalDateTime now, LocalDateTime due, long minutes) {
        long diff = Math.abs(ChronoUnit.MINUTES.between(now, due));
        return diff <= minutes;
    }

    private boolean isWithinHourWindow(LocalDateTime now, LocalDateTime due, long hours) {
        long diff = Math.abs(ChronoUnit.HOURS.between(now, due));
        return diff <= hours;
    }

    private boolean isWithinWindow(LocalDateTime now, LocalDateTime due, long days) {
        long diff = Math.abs(ChronoUnit.DAYS.between(now.toLocalDate(), due.toLocalDate()));
        return diff <= days;
    }

    @Transactional
    public Reminder save(ReminderForm form, String ownerEmail) {
        Reminder reminder = form.getId() == null
                ? new Reminder()
                : findByIdForOwner(form.getId(), ownerEmail);

        reminder.setTitle(form.getTitle().trim());
        reminder.setDescription(form.getDescription().trim());
        reminder.setCycle(form.getCycle());
        reminder.setCustomCycleDays(form.getCycle() == ReminderCycle.CUSTOM ? form.getCustomCycleDays() : null);
        reminder.setReminderDate(form.getReminderDate());
        reminder.setEmail(StringUtils.hasText(form.getEmail()) ? form.getEmail().trim() : null);
        reminder.setWhatsappNumber(StringUtils.hasText(form.getWhatsappNumber()) ? form.getWhatsappNumber().trim() : null);
        reminder.setOwnerEmail(ownerEmail);
        if (reminder.getId() == null) {
            reminder.setSilencedUntil(null);
            reminder.setActive(true);
        }
        return reminderRepository.save(reminder);
    }

    @Transactional
    public void acceptAndMoveToNextPeriod(Long id, String ownerEmail) {
        Reminder reminder = findByIdForOwner(id, ownerEmail);

        LocalDateTime nextDate = nextDate(reminder.getReminderDate(), reminder.getCycle(), reminder.getCustomCycleDays());
        reminder.setReminderDate(nextDate);
        reminder.setSilencedUntil(calculateSilenceUntil(nextDate, reminder.getCycle()));
        reminderRepository.save(reminder);
    }

    @Transactional
    public void toggleActive(Long id, String ownerEmail) {
        Reminder reminder = findByIdForOwner(id, ownerEmail);
        reminder.setActive(!reminder.isActive());
        reminderRepository.save(reminder);
    }

    @Transactional
    public void delete(Long id, String ownerEmail) {
        findByIdForOwner(id, ownerEmail);
        notificationLogRepository.deleteByReminderId(id);
        reminderRepository.deleteById(id);
    }

    @Transactional
    public Reminder duplicate(Long id, String ownerEmail) {
        Reminder source = findByIdForOwner(id, ownerEmail);

        Reminder copy = new Reminder();
        copy.setTitle(source.getTitle());
        copy.setDescription(source.getDescription());
        copy.setCycle(source.getCycle());
        copy.setCustomCycleDays(source.getCustomCycleDays());
        copy.setReminderDate(source.getReminderDate());
        copy.setEmail(source.getEmail());
        copy.setWhatsappNumber(source.getWhatsappNumber());
        copy.setOwnerEmail(source.getOwnerEmail());
        copy.setActive(source.isActive());
        copy.setSilencedUntil(null);
        return reminderRepository.save(copy);
    }

    private Reminder findByIdForOwner(Long id, String ownerEmail) {
        return reminderRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reminder not found"));
    }

    public LocalDateTime nextDate(LocalDateTime from, ReminderCycle cycle, Integer customDays) {
        return switch (cycle) {
            case MINUTELY -> from.plusMinutes(1);
            case HOURLY -> from.plusHours(1);
            case DAILY -> from.plusDays(1);
            case WEEKLY -> from.plusWeeks(1);
            case MONTHLY -> from.plusMonths(1);
            case YEARLY -> from.plusYears(1);
            case CUSTOM -> from.plusDays(customDays == null ? 1 : customDays);
        };
    }

    private LocalDateTime calculateSilenceUntil(LocalDateTime nextDate, ReminderCycle cycle) {
        if (cycle == ReminderCycle.MINUTELY || cycle == ReminderCycle.HOURLY || cycle == ReminderCycle.DAILY) {
            return nextDate;
        }
        return nextDate.minusDays(7);
    }

    public String buildMessage(Reminder reminder) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(now, reminder.getReminderDate());
        long days = ChronoUnit.DAYS.between(now.toLocalDate(), reminder.getReminderDate().toLocalDate());
        return "Reminder: " + reminder.getTitle()
                + "\nDescription: " + reminder.getDescription()
                + "\nDue Date: " + reminder.getReminderDate()
                + "\nCycle: " + reminder.getCycle()
                + "\nDays to due: " + days
                + "\nMinutes to due: " + minutes;
    }
}
