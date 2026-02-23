package com.reminders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RemindersApplication {
    public static void main(String[] args) {
        SpringApplication.run(RemindersApplication.class, args);
    }
}
