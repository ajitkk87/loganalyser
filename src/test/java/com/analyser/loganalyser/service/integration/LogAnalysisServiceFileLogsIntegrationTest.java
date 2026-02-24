package com.analyser.loganalyser.service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.analyser.loganalyser.service.LogAnalysisService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

@SpringBootTest
class LogAnalysisServiceFileLogsIntegrationTest {

    @Autowired private LogAnalysisService logAnalysisService;

    @Test
    void processLogs_shouldCallRealChatModel() throws IOException {
        if (!hasRealApiKey()) {
            System.out.println("Skipping integration test: real OpenAI key not configured");
            return;
        }

        // Given
        Path path = new ClassPathResource("dummy.log").getFile().toPath();
        String rawLogs = Files.readString(path);

        // When
        String result = logAnalysisService.processLogs(rawLogs);

        // Then
        System.out.println("LLM Response: " + result);
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
