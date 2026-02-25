package com.reminders.repository;

import com.reminders.model.Reminder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findAllByOwnerEmailOrderByReminderDateAsc(String ownerEmail);

    Optional<Reminder> findByIdAndOwnerEmail(Long id, String ownerEmail);

    @Query("""
            SELECT r
            FROM Reminder r
            WHERE r.active = true
              AND (r.silencedUntil IS NULL OR r.silencedUntil <= CURRENT_TIMESTAMP)
            """)
    List<Reminder> findReminderCandidates();
}
