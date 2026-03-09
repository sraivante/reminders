package com.reminders.model;

/**
 * Recurrence cycles for reminders, ranging from minutely to yearly,
 * with a custom option for user-defined intervals.
 */
public enum ReminderCycle {
    MINUTELY,
    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM
}
