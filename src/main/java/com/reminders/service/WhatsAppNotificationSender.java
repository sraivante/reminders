package com.reminders.service;

import com.reminders.model.Reminder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
public class WhatsAppNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificationSender.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.whatsapp.twilio.account-sid:}")
    private String accountSid;

    @Value("${app.whatsapp.twilio.auth-token:}")
    private String authToken;

    @Value("${app.whatsapp.twilio.from:}")
    private String fromWhatsapp;

    public boolean send(Reminder reminder, String messageBody) {
        String recipient = reminder.getWhatsappNumber();
        if (!StringUtils.hasText(recipient)
                || !StringUtils.hasText(accountSid)
                || !StringUtils.hasText(authToken)
                || !StringUtils.hasText(fromWhatsapp)) {
            return false;
        }

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        String authValue = Base64.getEncoder()
                .encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + authValue);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("From", "whatsapp:" + fromWhatsapp);
        body.add("To", "whatsapp:" + recipient);
        body.add("Body", messageBody);

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            return true;
        } catch (Exception ex) {
            log.error("WhatsApp send failed for reminder {}", reminder.getId(), ex);
            return false;
        }
    }
}
