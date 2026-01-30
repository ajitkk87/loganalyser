package com.omaap.loganalyser.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.ollama.OllamaChatModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogAnalysisServiceTest {

    @Mock
    private OllamaChatModel chatModel;

    @InjectMocks
    private LogAnalysisService logAnalysisService;

    @Test
    void processLogs_shouldCallChatModelAndReturnResponse() {
        // Given
        String rawLogs = "ERROR: Connection timeout";
        String expectedResponse = "The logs indicate a connection timeout error.";
        when(chatModel.call(anyString())).thenReturn(expectedResponse);

        // When
        String result = logAnalysisService.processLogs(rawLogs);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(chatModel).call("Identify errors in these logs: " + rawLogs);
    }
}
