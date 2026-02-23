package com.reminders.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseBootstrap {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBootstrap.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseBootstrap(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS reminder (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(120) NOT NULL,
                    description VARCHAR(500) NOT NULL,
                    cycle VARCHAR(20) NOT NULL,
                    custom_cycle_days INT NULL,
                    reminder_date DATETIME NOT NULL,
                    email VARCHAR(120) NULL,
                    whatsapp_number VARCHAR(30) NULL,
                    active BIT NOT NULL DEFAULT b'1',
                    silenced_until DATETIME NULL,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                ALTER TABLE reminder
                MODIFY COLUMN reminder_date DATETIME NOT NULL
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS notification_log (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    reminder_id BIGINT NOT NULL,
                    channel VARCHAR(20) NOT NULL,
                    sent_on DATE NULL,
                    slot_key VARCHAR(30) NULL,
                    sent_at DATETIME NOT NULL,
                    CONSTRAINT uk_notification_slot UNIQUE (reminder_id, channel, slot_key),
                    CONSTRAINT fk_notification_reminder FOREIGN KEY (reminder_id) REFERENCES reminder (id)
                )
                """);

        trySql("ALTER TABLE notification_log ADD COLUMN slot_key VARCHAR(30) NULL");
        trySql("CREATE UNIQUE INDEX uk_notification_slot ON notification_log (reminder_id, channel, slot_key)");
    }

    private void trySql(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ex) {
            log.debug("Skipping SQL migration [{}]: {}", sql, ex.getMessage());
        }
    }
}
