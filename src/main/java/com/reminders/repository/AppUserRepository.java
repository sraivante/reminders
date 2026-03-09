package com.reminders.repository;

import com.reminders.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link AppUser} entities.
 * Uses the user's email as the primary key.
 */
public interface AppUserRepository extends JpaRepository<AppUser, String> {
}
