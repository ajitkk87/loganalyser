package com.omaap.loganalyser.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LogAnalysisServiceIntegrationTest {

    @Autowired
    private LogAnalysisService logAnalysisService;

    @Test
    void processLogs_shouldCallRealChatModel() throws IOException {
        // Given
        Path path = new ClassPathResource("dummy.log").getFile().toPath();
        String rawLogs = Files.readString(path);

        // When
        String result = logAnalysisService.processLogs(rawLogs);

        // Then
        System.out.println("LLM Response: " + result);
        assertThat(result).isNotBlank();
    }
}
