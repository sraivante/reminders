package com.reminders.service;

import com.reminders.model.AppUser;
import com.reminders.repository.AppUserRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
