package com.reminders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RemindersApplication {
    private static final String ANSI_BOLD_YELLOW = "\u001B[1;33m";
    private static final String ANSI_RESET = "\u001B[0m";

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RemindersApplication.class);
        app.addListeners((ApplicationEnvironmentPreparedEvent event) -> {
            String dbUrl = event.getEnvironment().getProperty("spring.datasource.url", "NOTHING SET");
            System.out.println(ANSI_BOLD_YELLOW + "Configured spring.datasource.url: " + dbUrl + ANSI_RESET);
        });
        app.run(args);
    }
}
