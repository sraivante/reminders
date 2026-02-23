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
);

ALTER TABLE reminder
    MODIFY COLUMN reminder_date DATETIME NOT NULL;

CREATE TABLE IF NOT EXISTS notification_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reminder_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    slot_key VARCHAR(30) NOT NULL,
    sent_at DATETIME NOT NULL,
    CONSTRAINT uk_notification_slot UNIQUE (reminder_id, channel, slot_key),
    CONSTRAINT fk_notification_reminder FOREIGN KEY (reminder_id) REFERENCES reminder (id)
);

-- Legacy compatibility: older databases may still have sent_on as NOT NULL.
SET @has_sent_on := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'notification_log'
      AND COLUMN_NAME = 'sent_on'
);
SET @sql_sent_on := IF(
    @has_sent_on > 0,
    'ALTER TABLE notification_log MODIFY COLUMN sent_on DATE NULL',
    'SELECT 1'
);
PREPARE stmt_sent_on FROM @sql_sent_on;
EXECUTE stmt_sent_on;
DEALLOCATE PREPARE stmt_sent_on;
