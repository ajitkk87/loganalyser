package com.analyser.loganalyser.service;

import com.analyser.loganalyser.config.LogProperties;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LogAnalysisService {

    private static final int MAX_LOG_LENGTH = 1_000_000;
    private static final int MAX_QUERY_LENGTH = 100_000;
    private static final String LOG_OUTPUT_DIR = "output/log_analysis_output";
    private static final String EMAIL_OUTPUT_DIR = "output/email";

    private static final String GUARDRAILS_FILE = "templates/guardrails.st";
    private static final String EMAIL_TEMPLATE_FILE = "templates/email-alert.st";

    private final ChatModel chatModel;
    private final String guardrailsContent;
    private final String emailTemplateContent;
    private final RestTemplate restTemplate;
    private final LogProperties logProperties;

    // The starter automatically creates this bean from your yaml settings
    public LogAnalysisService(ChatModel chatModel, LogProperties logProperties) {
        this.chatModel = chatModel;
        this.logProperties = logProperties;
        this.restTemplate = new RestTemplate();
        String loaded = "";
        try {
            ClassPathResource resource = new ClassPathResource(GUARDRAILS_FILE);
            try (InputStream in = resource.getInputStream()) {
                loaded = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("Warning: could not load guardrails file: " + e.getMessage());
        }
        this.guardrailsContent = loaded;

        String emailLoaded = "";
        try {
            ClassPathResource resource = new ClassPathResource(EMAIL_TEMPLATE_FILE);
            try (InputStream in = resource.getInputStream()) {
                emailLoaded = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("Warning: could not load email template file: " + e.getMessage());
        }
        this.emailTemplateContent = emailLoaded;
    }

    public String processLogs(String rawLogs) {
        return processLogs(rawLogs, null, null, null, null, null, null);
    }

    public String processLogs(String rawLogs, String query) {
        return processLogs(rawLogs, query, null, null, null, null, null);
    }

    public String processLogs(String rawLogs, String query, String repoLink) {
        return processLogs(rawLogs, query, repoLink, null, null, null, null);
    }

    public String processLogs(String rawLogs, String query, String repoLink, String logLevel) {
        return processLogs(rawLogs, query, repoLink, logLevel, null, null, null);
    }

    public String processLogs(
            String rawLogs, String query, String repoLink, String logLevel, Integer days) {
        return processLogs(rawLogs, query, repoLink, logLevel, days, null, null);
    }

    public String processLogs(
            String rawLogs,
            String query,
            String repoLink,
            String logLevel,
            Integer days,
            String applicationName) {
        return processLogs(rawLogs, query, repoLink, logLevel, days, applicationName, null);
    }

    public String processLogs(
            String rawLogs,
            String query,
            String repoLink,
            String logLevel,
            Integer days,
            String applicationName,
            String env) {
        String logsToProcess = rawLogs;
        if (env != null && !env.trim().isEmpty()) {
            logsToProcess = fetchLogsFromEnv(env, days, logLevel, applicationName);
        }

        if (logsToProcess != null && logsToProcess.length() > MAX_LOG_LENGTH) {
            throw new IllegalArgumentException(
                    "Raw logs length exceeds the limit of " + MAX_LOG_LENGTH + " characters.");
        }

        if (query != null && query.length() > MAX_QUERY_LENGTH) {
            throw new IllegalArgumentException(
                    "Query length exceeds the limit of " + MAX_QUERY_LENGTH + " characters.");
        }

        StringBuilder prompt = new StringBuilder();
        if (query != null && !query.isEmpty()) {
            prompt.append(query).append(" ");
        }

        if (logLevel != null && !logLevel.trim().isEmpty() && !"All".equalsIgnoreCase(logLevel)) {
            prompt.append("Analyze these logs focusing on ")
                    .append(logLevel)
                    .append(" level entries");
        } else {
            prompt.append("Identify errors in these logs");
        }

        if (applicationName != null
                && !applicationName.trim().isEmpty()
                && !"All".equalsIgnoreCase(applicationName)) {
            prompt.append(" for application ").append(applicationName);
        }

        if (days != null) {
            prompt.append(" from the last ").append(days).append(" days");
        }

        if (repoLink != null && !repoLink.trim().isEmpty()) {
            prompt.append(". Context repository: ").append(repoLink);
        }
        prompt.append(
                "Avoid duplicate errors and Provide the output in a consistent tabular format with the following columns: Exception, Impacted Class, Details of Exception, Remediation of Code.");

        prompt.append(": ").append(logsToProcess);

        // Prepend guardrails content (if available) as system-like instructions
        /*
        if (guardrailsContent != null && !guardrailsContent.isEmpty()) {
            prompt.append("[GUARDRAILS]\n" + guardrailsContent + "\n\n");
        }
        */

        // Call chat model with combined prompt
        String result = this.chatModel.call(prompt.toString());

        // Save output to file
        saveOutputToFile(result);

        // Send email alert if errors are found in the result
        if (result != null && (result.contains("ERROR") || result.contains("Exception"))) {
            StringBuilder emailPrompt = new StringBuilder();
            emailPrompt.append("Use the following email template for the alert:\n");
            emailPrompt.append(emailTemplateContent).append("\n\n");
            emailPrompt.append(
                    "Fill in the placeholders {applicationName}, {environment}, {date}, and {errorDetails} based on the logs analyzed.\n\n");
            emailPrompt.append("Analysis Result:\n").append(result);
            // Call chat model with combined prompt
            String emailResult = this.chatModel.call(emailPrompt.toString());

            sendEmailAlert("Log Analysis Error Alert", emailResult);
        }
        return result;
    }

    /**
     * Sends an email alert with the analysis results. In a real application, use Spring's
     * JavaMailSender.
     */
    private void sendEmailAlert(String subject, String body) {
        System.out.println("Sending email alert...");
        System.out.println("Subject: " + subject);
        // Mock implementation of email sending logic
        // Example with JavaMailSender:
        // SimpleMailMessage message = new SimpleMailMessage();
        // message.setTo("admin@example.com");
        // message.setSubject(subject);
        // message.setText(body);
        // mailSender.send(message);
        try {
            Files.createDirectories(Paths.get(EMAIL_OUTPUT_DIR + "/email"));
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");
            String emailFileName =
                    String.format("%s/email/email_%s.txt", EMAIL_OUTPUT_DIR, now.format(formatter));

            try (FileWriter writer = new FileWriter(emailFileName)) {
                writer.write("Subject: " + subject + "\n");
                writer.write("Body:\n" + body);
            }
            System.out.println("Email content saved to: " + emailFileName);
        } catch (IOException e) {
            System.err.println("Error saving email to file: " + e.getMessage());
        }
        System.out.println("Email alert sent successfully.");
    }

    /**
     * Mock implementation to fetch logs from a specified environment. In a real application, this
     * would connect to a log management system like ELK, Splunk, or a cloud provider's logging
     * service.
     */
    private String fetchLogsFromEnv(
            String env, Integer days, String logLevel, String applicationName) {
        StringBuilder mockLogs = new StringBuilder();
        mockLogs.append("Fetching logs from '").append(env).append("' environment");

        if (days != null) {
            mockLogs.append(" for the last ").append(days).append(" days");
        } else {
            mockLogs.append(" for the last 1 days");
        }

        if (applicationName != null
                && !applicationName.trim().isEmpty()
                && !"All".equalsIgnoreCase(applicationName)) {
            mockLogs.append(" for application '").append(applicationName).append("'");
        }

        if (logLevel != null && !logLevel.trim().isEmpty() && !"All".equalsIgnoreCase(logLevel)) {
            mockLogs.append(" with level '").append(logLevel).append("'");
        }

        mockLogs.append("...\n");

        Map<String, String> envUrls = logProperties.getEnvUrls();
        if (envUrls != null && envUrls.containsKey(env)) {
            String baseUrl = envUrls.get(env);
            String url =
                    String.format(
                            "%s/api/logs?days=%d&level=%s&app=%s",
                            baseUrl, days != null ? days : 1, logLevel, applicationName);
            try {
                // return restTemplate.getForObject(url, String.class);
            } catch (Exception e) {
                mockLogs.append("Error fetching from remote URL: ")
                        .append(e.getMessage())
                        .append("\n");
            }
        }

        // Mock log data for demonstration purposes
        mockLogs.append("2024-01-01 10:00:00 INFO: Application startup successful.\n");
        mockLogs.append(
                "2024-01-01 10:05:00 ERROR: NullPointerException at com.example.UserService.getUser(UserService.java:101)\n");
        mockLogs.append("2024-01-01 10:10:00 WARN: Deprecated API usage detected.");

        return mockLogs.toString();
    }

    /**
     * Saves the chatModel output to a file with a timestamp. Files are saved in the
     * log_analysis_output directory.
     */
    private void saveOutputToFile(String output) {
        try {
            // Create output directory if it doesn't exist
            Files.createDirectories(Paths.get(LOG_OUTPUT_DIR));

            // Generate timestamp-based filename
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");
            String fileName =
                    String.format("%s/analysis_%s.txt", LOG_OUTPUT_DIR, now.format(formatter));

            // Write output to file
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write("Log Analysis Output\n");
                writer.write("Generated at: " + now + "\n");
                writer.write("=".repeat(80) + "\n\n");
                writer.write(output);
            }

            System.out.println("Analysis output saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving output to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
