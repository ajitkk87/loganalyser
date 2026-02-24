package com.analyser.loganalyser.service;

import com.analyser.loganalyser.config.LogProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EnvApiLogFetcher implements LogFetcher {

    private final LogProperties logProperties;

    public EnvApiLogFetcher(LogProperties logProperties) {
        this.logProperties = logProperties;
    }

    @Override
    public String fetchLogs(String env, Integer days, String logLevel, String applicationName) {
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
            mockLogs.append("Source URL: ").append(url).append("\n");
        }

        mockLogs.append("2024-01-01 10:00:00 INFO: Application startup successful.\n");
        mockLogs.append(
                "2024-01-01 10:05:00 ERROR: NullPointerException at com.example.UserService.getUser(UserService.java:101)\n");
        mockLogs.append("2024-01-01 10:10:00 WARN: Deprecated API usage detected.");
        return mockLogs.toString();
    }
}
