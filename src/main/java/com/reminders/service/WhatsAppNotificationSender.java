package com.reminders.service;

import com.reminders.model.Reminder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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

/**
 * Sends reminder notifications via WhatsApp using the Facebook Graph API.
 * Formats messages with WhatsApp bold markdown and normalizes Indian phone numbers.
 */
@Component
public class WhatsAppNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificationSender.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.whatsapp.twilio.account-sid:1234}")
    private String accountSid;

    @Value("${app.whatsapp.twilio.auth-token:EAAnb3q49HIUBQ6NTZBY0rRjyC5rhA7KhAL756WDZAdIZAnstrFgfpATF9ZAiRSkduEsSLAbaU1XrkMCPBgczSuvLlvGbIlP2ZAEZBcApNtGVUsMco2yPTDaBcoyZBbRleodMJn06qGdoL2AmxVeRrFyg4ohUH1xULZC9yWAHoQ5J8lQpgRrELWt9by7uU8iOniSActxdrS5MiiCUNVJzJ3zxExioFQdPchCpo2JUVPdPW40a68hDIVie46FZBok0X5rsPfOM2ZB2NlrydRPAIb6t7vqAZDZD}")
    private String authToken;

    @Value("${app.whatsapp.twilio.from:1025774503953615}")
    private String fromWhatsapp;

    /**
     * Sends a WhatsApp text message for the given reminder.
     *
     * @param reminder    the reminder whose WhatsApp recipient number is used
     * @param messageBody plain-text message body to be formatted for WhatsApp
     * @return {@code true} if the message was sent successfully, {@code false} otherwise
     */
    public boolean send(Reminder reminder, String messageBody) {

        String recipient = reminder.getWhatsappNumber();

        if (!StringUtils.hasText(recipient)) {
            return false;
        }

        String url = "https://graph.facebook.com/v22.0/" + fromWhatsapp + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // IMPORTANT: Use Map instead of String JSON
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", normalizeNumber(recipient));
        body.put("type", "text");

        Map<String, String> text = new HashMap<>();
        text.put("body", formatForWhatsApp(messageBody));

        body.put("text", text);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForObject(url, request, String.class);
            return true;
        } catch (Exception ex) {
            log.error("WhatsApp send failed for reminder {}", reminder.getId(), ex);
            return false;
        }
    }

    private String normalizeNumber(String number) {
        number = number.replaceAll("\\D", "");

        if (number.startsWith("91")) {
            return number;
        }
        return "91" + number;
    }

    private String formatForWhatsApp(String messageBody) {
        StringBuilder formatted = new StringBuilder("*REMINDER ALERT*\n");
        for (String line : messageBody.split("\\r?\\n")) {
            if (!StringUtils.hasText(line)) {
                continue;
            }
            int separator = line.indexOf(':');
            if (separator > 0 && separator < line.length() - 1) {
                String label = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                if ("Reminder".equalsIgnoreCase(label) || "Title".equalsIgnoreCase(label)) {
                    formatted.append("*").append(label).append(":* ").append("*").append(value).append("*\n");
                } else {
                    formatted.append("*").append(label).append(":* ").append(value).append("\n");
                }
            } else {
                formatted.append(line).append("\n");
            }
        }
        return formatted.toString().trim();
    }
}
