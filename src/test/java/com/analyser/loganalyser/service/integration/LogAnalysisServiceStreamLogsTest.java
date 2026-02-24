package com.analyser.loganalyser.service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.analyser.loganalyser.config.LogProperties;
import com.analyser.loganalyser.model.LogAnalysisRequest;
import com.analyser.loganalyser.service.LogAnalysisService;
import com.analyser.loganalyser.util.LogStreamGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableConfigurationProperties(LogProperties.class)
class LogAnalysisServiceStreamLogsTest {

    @Autowired private LogAnalysisService logAnalysisService;

    @Test
    void processLogs_shouldAnalyzeGeneratedLogsWithRealModel() {
        if (!hasRealApiKey()) {
            System.out.println("Skipping integration test: real OpenAI key not configured");
            return;
        }

        // Given
        // Generate 10 log lines using the LogStreamGenerator
        String rawLogs =
                IntStream.range(0, 30)
                        .mapToObj(i -> LogStreamGenerator.generateLogLine())
                        .collect(Collectors.joining("\n"));

        System.out.println("Generated Logs:\n" + rawLogs);

        // When
        String result = logAnalysisService.processLogs(rawLogs);

        // Then
        System.out.println("LLM Response:\n" + result);
        assertThat(result).isNotBlank();
        // We can't assert the exact content since it's from a real LLM, but we can check if it's
        // not empty
    }

    @Test
    void processLogs_shouldAnalyzeGeneratedLogsWithFiltersWithRealModel() {
        if (!hasRealApiKey()) {
            System.out.println("Skipping integration test: real OpenAI key not configured");
            return;
        }

        // Given
        String rawLogs =
                IntStream.range(0, 30)
                        .mapToObj(i -> LogStreamGenerator.generateLogLine())
                        .collect(Collectors.joining("\n"));

        System.out.println("Generated Logs for Filter Test:\n" + rawLogs);

        String promptQuery = "Find Most Frequent ERROR);";

        // When
        String result =
                logAnalysisService.processLogs(
                        new LogAnalysisRequest(rawLogs, promptQuery, null, null, null, null, null));

        // Then
        System.out.println("LLM Response (Filtered):\n" + result);
        assertThat(result).isNotBlank();
    }

    private boolean hasRealApiKey() {
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            key = System.getenv("OPEN_API_KEY");
        }
        return key != null && !key.isBlank() && !"test-key".equals(key);
    }
}
