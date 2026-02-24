package com.analyser.loganalyser.service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailAlertService {

    private static final Logger logger = LoggerFactory.getLogger(EmailAlertService.class);
    private static final String EMAIL_OUTPUT_DIR = "output/email";

    public void sendEmailAlert(String subject, String body) {
        logger.info("Sending email alert with subject: {}", subject);
        try {
            Files.createDirectories(Paths.get(EMAIL_OUTPUT_DIR + "/email"));
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");
            String emailFileName =
                    String.format("%s/email/email_%s.txt", EMAIL_OUTPUT_DIR, now.format(formatter));

            try (FileWriter writer = new FileWriter(emailFileName, StandardCharsets.UTF_8)) {
                writer.write("Subject: " + subject + "\n");
                writer.write("Body:\n" + body);
            }
            logger.info("Email content saved to: {}", emailFileName);
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error saving email to file: {}", e.getMessage(), e);
            }
        }
        logger.info("Email alert sent successfully.");
    }
}
