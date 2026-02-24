# Spring Boot 3 Reminders App

Spring Boot 3 + Thymeleaf + MySQL app for recurring reminders with email and WhatsApp notifications.

## Features
- MySQL-backed `reminder` data in `reminders` database.
- Reminder fields: title, description, cycle (`MINUTELY`, `HOURLY`, `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`, `CUSTOM`), reminder date-time.
- Notification windows by cycle:
  - `MINUTELY`: every minute once due.
  - `HOURLY`: eligible in due time `+/-10 minutes`, checked every minute.
  - `DAILY`: eligible in due time `+/-5 hours`, sent hourly.
  - `WEEKLY`: daily notifications from due date -3 days to due date +3 days.
  - `MONTHLY`: daily notifications from due date -5 days to due date +5 days.
  - `YEARLY`: daily notifications from due date -10 days to due date +10 days.
  - `CUSTOM`: daily notifications from due date -7 days to due date +7 days.
- Accept action silences current reminder period and moves reminder to next period automatically.
- Notification dedup by slot/channel using `notification_log`:
  - minute slot for `MINUTELY` and `HOURLY`
  - hour slot for `DAILY`
  - day slot for remaining cycles.

## Tech
- Java 21
- Spring Boot 3.5.3
- Spring Data JPA
- Thymeleaf UI
- MySQL connector
- JavaMailSender (email)
- Twilio REST API (WhatsApp)

## Run
1. Create/update MySQL credentials in `src/main/resources/application.properties`.
2. Set SMTP settings for email in `src/main/resources/application.properties`.
3. Set Twilio WhatsApp values:
   - `app.whatsapp.twilio.account-sid`
   - `app.whatsapp.twilio.auth-token`
   - `app.whatsapp.twilio.from`
4. Build and run:
   - `mvn spring-boot:run`
5. Open:
   - `http://localhost:8080`

## Docker Compose (Raspberry Pi + MySQL volume)
- Uses local MySQL data folder mapping: `./mysql_db:/var/lib/mysql`
- Build and run:
  1. `docker compose up -d --build`
  2. Open `http://<raspberry-pi-ip>:8080`
- Stop:
  - `docker compose down`
- Optional SMTP/WhatsApp environment values:
  - `MAIL_USERNAME`
  - `MAIL_PASSWORD`
  - `MAIL_FROM`
  - `WHATSAPP_FROM`

Files added for containerized run:
- `Dockerfile`
- `docker-compose.yml`
- `.dockerignore`

## Notes
- This project uses `spring.jpa.hibernate.ddl-auto=update`, so tables are auto-created.
- Scheduler runs every minute by default (`app.notification.cron=0 * * * * *`).
- Twilio WhatsApp destination should be in E.164 format, ex: `+14155552671`.
