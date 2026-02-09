package com.analyser.loganalyser.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LogStreamGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Random RANDOM = new Random();

    private static final List<String> INFO_MESSAGES =
            List.of(
                    "Starting application",
                    "User logged in",
                    "Request processed successfully",
                    "Cache refreshed",
                    "Health check passed",
                    "Scheduled task executed",
                    "Data sync completed");

    private static final List<String> WARN_MESSAGES =
            List.of(
                    "Retrying connection - com.example.network.ConnectionManager",
                    "High memory usage detected - com.example.monitor.ResourceMonitor",
                    "Response time > 500ms - com.example.api.RequestFilter",
                    "Deprecated API usage - com.example.legacy.LegacyService",
                    "Disk space running low - com.example.system.DiskMonitor",
                    "Connection pool nearing limit - com.zaxxer.hikari.HikariPool");

    private static final List<String> ERROR_MESSAGES =
            List.of(
                    "Connection refused to database - java.net.ConnectException",
                    "NullPointerException at com.example.Service.process(Service.java:25) - java.lang.NullPointerException",
                    "Timeout waiting for upstream service - java.util.concurrent.TimeoutException",
                    "Failed to write to file system - java.io.IOException",
                    "Payment gateway unavailable - com.example.payment.GatewayException",
                    "Transaction rollback failed - org.springframework.transaction.TransactionSystemException",
                    "OutOfMemoryError: Java heap space - java.lang.OutOfMemoryError");

    public static void main(String[] args) {
        System.out.println("Generating log stream... Press Ctrl+C to stop.");

        try {
            while (true) {
                String log = generateLogLine();
                System.out.println(log);
                // Sleep for random duration between 200ms and 1500ms
                TimeUnit.MILLISECONDS.sleep(200 + RANDOM.nextInt(1300));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Log generation stopped.");
        }
    }

    public static String generateLogLine() {
        String level = getRandomLevel();
        String message = getRandomMessage(level);
        String timestamp = LocalDateTime.now().format(FORMATTER);
        return String.format("%s %s %s", timestamp, level, message);
    }

    private static String getRandomLevel() {
        int roll = RANDOM.nextInt(100);
        if (roll < 70) return "INFO";
        if (roll < 90) return "WARN";
        return "ERROR";
    }

    private static String getRandomMessage(String level) {
        return switch (level) {
            case "INFO" -> INFO_MESSAGES.get(RANDOM.nextInt(INFO_MESSAGES.size()));
            case "WARN" -> WARN_MESSAGES.get(RANDOM.nextInt(WARN_MESSAGES.size()));
            case "ERROR" -> ERROR_MESSAGES.get(RANDOM.nextInt(ERROR_MESSAGES.size()));
            default -> "Unknown";
        };
    }
}
