package com.analyser.loganalyser.service;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
public class LogAnalysisService {

    private static final int MAX_LOG_LENGTH = 1_000_000;
    private static final int MAX_QUERY_LENGTH = 100_000;

    private final OllamaChatModel chatModel;

    // The starter automatically creates this bean from your yaml settings
    public LogAnalysisService(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
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

    public String processLogs(String rawLogs, String query, String repoLink, String logLevel, Integer days) {
        return processLogs(rawLogs, query, repoLink, logLevel, days, null, null);
    }

    public String processLogs(String rawLogs, String query, String repoLink, String logLevel, Integer days, String applicationName) {
        return processLogs(rawLogs, query, repoLink, logLevel, days, applicationName, null);
    }

    public String processLogs(String rawLogs, String query, String repoLink, String logLevel, Integer days, String applicationName, String env) {
        String logsToProcess = rawLogs;
        if (env != null && !env.trim().isEmpty()) {
            logsToProcess = fetchLogsFromEnv(env, days, logLevel, applicationName);
        }

        if (logsToProcess != null && logsToProcess.length() > MAX_LOG_LENGTH) {
            throw new IllegalArgumentException("Raw logs length exceeds the limit of " + MAX_LOG_LENGTH + " characters.");
        }

        if (query != null && query.length() > MAX_QUERY_LENGTH) {
            throw new IllegalArgumentException("Query length exceeds the limit of " + MAX_QUERY_LENGTH + " characters.");
        }

        StringBuilder prompt = new StringBuilder();
        if (query != null && !query.isEmpty()) {
            prompt.append(query).append(" ");
        }
        
        if (logLevel != null && !logLevel.trim().isEmpty() && !"All".equalsIgnoreCase(logLevel)) {
            prompt.append("Analyze these logs focusing on ").append(logLevel).append(" level entries");
        } else {
            prompt.append("Identify errors in these logs");
        }

        if (applicationName != null && !applicationName.trim().isEmpty() && !"All".equalsIgnoreCase(applicationName)) {
            prompt.append(" for application ").append(applicationName);
        }

        if (days != null) {
            prompt.append(" from the last ").append(days).append(" days");
        }

        if (repoLink != null && !repoLink.trim().isEmpty()) {
            prompt.append(". Context repository: ").append(repoLink);
        }

        prompt.append(". Provide the output in a consistent tabular format with the following columns: Exception, Impacted Class, Details of Exception, Remediation of Code.");

        prompt.append(": ").append(logsToProcess);
        return chatModel.call(prompt.toString());
    }

    /**
     * Mock implementation to fetch logs from a specified environment.
     * In a real application, this would connect to a log management system like ELK, Splunk, or a cloud provider's logging service.
     */
    private String fetchLogsFromEnv(String env, Integer days, String logLevel, String applicationName) {
        StringBuilder mockLogs = new StringBuilder();
        mockLogs.append("Fetching logs from '").append(env).append("' environment");

        if (days != null) {
            mockLogs.append(" for the last ").append(days).append(" days");
        } else {
            mockLogs.append(" for the last 1 days");
        }

        if (applicationName != null && !applicationName.trim().isEmpty() && !"All".equalsIgnoreCase(applicationName)) {
            mockLogs.append(" for application '").append(applicationName).append("'");
        }

        if (logLevel != null && !logLevel.trim().isEmpty() && !"All".equalsIgnoreCase(logLevel)) {
            mockLogs.append(" with level '").append(logLevel).append("'");
        }

        mockLogs.append("...\n");

        // Mock log data for demonstration purposes
        mockLogs.append("2024-01-01 10:00:00 INFO: Application startup successful.\n");
        mockLogs.append("2024-01-01 10:05:00 ERROR: NullPointerException at com.example.UserService.getUser(UserService.java:101)\n");
        mockLogs.append("2024-01-01 10:10:00 WARN: Deprecated API usage detected.");

        return mockLogs.toString();
    }
}
