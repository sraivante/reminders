package com.reminders.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseBootstrap {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBootstrap.class);
    private static final String ANSI_BOLD_CYAN = "\u001B[1;36m";
    private static final String ANSI_RESET = "\u001B[0m";
    private final JdbcTemplate jdbcTemplate;
    private final String datasourceUrl;

    public DatabaseBootstrap(
            JdbcTemplate jdbcTemplate,
            @Value("${spring.datasource.url}") String datasourceUrl
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.datasourceUrl = datasourceUrl;
    }

    @PostConstruct
    public void ensureTables() {
        log.info("{}Configured DB URL: {}{}", ANSI_BOLD_CYAN, datasourceUrl, ANSI_RESET);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS app_user (
                    email VARCHAR(120) PRIMARY KEY,
                    password_hash VARCHAR(100) NOT NULL,
                    created_at DATETIME NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS reminder (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(120) NOT NULL,
                    description VARCHAR(500) NOT NULL,
                    cycle VARCHAR(20) NOT NULL,
                    custom_cycle_days INT NULL,
                    reminder_date DATETIME NOT NULL,
                    email VARCHAR(120) NULL,
                    owner_email VARCHAR(120) NOT NULL,
                    whatsapp_number VARCHAR(30) NULL,
                    active BIT NOT NULL DEFAULT b'1',
                    silenced_until DATETIME NULL,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS notification_log (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    reminder_id BIGINT NOT NULL,
                    channel VARCHAR(20) NOT NULL,
                    slot_key VARCHAR(30) NOT NULL,
                    sent_at DATETIME NOT NULL,
                    CONSTRAINT uk_notification_slot UNIQUE (reminder_id, channel, slot_key),
                    CONSTRAINT fk_notification_reminder FOREIGN KEY (reminder_id) REFERENCES reminder (id)
                )
                """);
    }
}
