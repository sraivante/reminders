package com.reminders.service;

import com.reminders.model.AppUser;
import com.reminders.repository.AppUserRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user authentication operations including signup and login.
 * Passwords are hashed with BCrypt before storage.
 */
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with the given email and password.
     *
     * @param rawEmail    the user's email address (will be normalized)
     * @param rawPassword the plaintext password to hash and store
     * @return the newly created {@link AppUser}
     * @throws IllegalArgumentException if the email is already registered
     */
    public AppUser signup(String rawEmail, String rawPassword) {
        String email = normalizeEmail(rawEmail);
        if (appUserRepository.existsById(email)) {
            throw new IllegalArgumentException("Email already registered.");
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return appUserRepository.save(user);
    }

    /**
     * Authenticates a user by email and password.
     *
     * @param rawEmail    the user's email address (will be normalized)
     * @param rawPassword the plaintext password to verify
     * @return the authenticated {@link AppUser}
     * @throws IllegalArgumentException if the credentials are invalid
     */
    public AppUser login(String rawEmail, String rawPassword) {
        String email = normalizeEmail(rawEmail);
        AppUser user = appUserRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return user;
    }

    public String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
