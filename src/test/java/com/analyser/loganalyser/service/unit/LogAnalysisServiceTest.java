package com.analyser.loganalyser.service.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.analyser.loganalyser.model.LogAnalysisRequest;
import com.analyser.loganalyser.service.AnalysisOutputStore;
import com.analyser.loganalyser.service.EmailAlertService;
import com.analyser.loganalyser.service.LogAnalysisPromptBuilder;
import com.analyser.loganalyser.service.LogAnalysisService;
import com.analyser.loganalyser.service.LogFetcher;
import com.analyser.loganalyser.service.PromptTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

@ExtendWith(MockitoExtension.class)
class LogAnalysisServiceTest {

    @Mock private ChatClient chatClient;
    @Mock private ChatClient.ChatClientRequestSpec chatClientRequestSpec;
    @Mock private ChatClient.CallResponseSpec callResponseSpec;
    @Mock private LogAnalysisPromptBuilder promptBuilder;
    @Mock private LogFetcher logFetcher;
    @Mock private EmailAlertService emailAlertService;
    @Mock private AnalysisOutputStore analysisOutputStore;
    @Mock private PromptTemplateService promptTemplateService;
    private LogAnalysisService logAnalysisService;

    @BeforeEach
    void setUp() {
        logAnalysisService =
                new LogAnalysisService(
                        chatClient,
                        promptBuilder,
                        logFetcher,
                        emailAlertService,
                        analysisOutputStore,
                        promptTemplateService);
        lenient().when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        lenient().when(chatClientRequestSpec.system(anyString())).thenReturn(chatClientRequestSpec);
        lenient().when(chatClientRequestSpec.user(anyString())).thenReturn(chatClientRequestSpec);
        lenient().when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        lenient().when(callResponseSpec.content()).thenReturn("ok");
    }

    @Test
    void processLogs_shouldCallChatModelAndReturnResponse() {
        // Given
        String rawLogs = "ERROR: Connection timeout";
        String expectedResponse = "The logs indicate a connection timeout error.";
        when(promptBuilder.buildAnalysisPrompt(any(), anyString()))
                .thenAnswer(
                        i ->
                                "Identify errors in these logs. Provide the output in a consistent tabular format: "
                                        + i.getArgument(1, String.class));
        when(promptTemplateService.guardrailsTemplate()).thenReturn("guardrails");
        when(callResponseSpec.content()).thenReturn(expectedResponse);

        // When
        String result = logAnalysisService.processLogs(rawLogs);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatClientRequestSpec).system("guardrails");
        verify(chatClientRequestSpec).user(promptCaptor.capture());
        assertThat(promptCaptor.getValue()).contains("Identify errors in these logs");
        assertThat(promptCaptor.getValue()).contains(rawLogs);
        assertThat(promptCaptor.getValue())
                .contains("Provide the output in a consistent tabular format");
    }

    @Test
    void processLogs_withRepoLink_shouldIncludeRepoLinkInPrompt() {
        // Given
        String rawLogs = "ERROR: Connection timeout";
        String repoLink = "https://github.com/example/repo";
        String expectedResponse = "Analysis with repo context.";
        when(promptBuilder.buildAnalysisPrompt(any(), anyString()))
                .thenReturn("Context repository: https://github.com/example/repo");
        when(promptTemplateService.guardrailsTemplate()).thenReturn("");
        when(callResponseSpec.content()).thenReturn(expectedResponse);

        // When
        String result =
                logAnalysisService.processLogs(
                        new LogAnalysisRequest(rawLogs, null, repoLink, null, null, null, null));

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatClientRequestSpec).user(promptCaptor.capture());
        assertThat(promptCaptor.getValue())
                .contains("Context repository: https://github.com/example/repo");
    }

    @Test
    void processLogs_withEnv_shouldFetchLogsFromEnv() {
        // Given
        String env = "PROD";
        String expectedResponse = "Analysis of PROD logs.";
        when(logFetcher.fetchLogs("PROD", null, null, null))
                .thenReturn("Fetching logs from 'PROD' environment");
        when(promptBuilder.buildAnalysisPrompt(any(), anyString()))
                .thenAnswer(i -> "prompt: " + i.getArgument(1, String.class));
        when(promptTemplateService.guardrailsTemplate()).thenReturn("");
        when(callResponseSpec.content()).thenReturn(expectedResponse);

        // When
        String result =
                logAnalysisService.processLogs(
                        new LogAnalysisRequest(null, null, null, null, null, null, env));

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatClientRequestSpec).user(promptCaptor.capture());
        // Verify that the mock logs from fetchLogsFromEnv are included
        assertThat(promptCaptor.getValue()).contains("Fetching logs from 'PROD' environment");
    }

    @Test
    void processLogs_withEnvAndFilters_shouldIncludeFiltersInMockLogs() {
        // Given
        String env = "PROD";
        String logLevel = "ERROR";
        String appName = "UserService";
        int days = 3;
        String expectedResponse = "Analysis of filtered PROD logs.";
        when(logFetcher.fetchLogs("PROD", 3, "ERROR", "UserService"))
                .thenReturn(
                        "Fetching logs from 'PROD' environment for the last 3 days for application 'UserService' with level 'ERROR'");
        when(promptBuilder.buildAnalysisPrompt(any(), anyString()))
                .thenAnswer(i -> "prompt: " + i.getArgument(1, String.class));
        when(promptTemplateService.guardrailsTemplate()).thenReturn("");
        when(callResponseSpec.content()).thenReturn(expectedResponse);

        // When
        String result =
                logAnalysisService.processLogs(
                        new LogAnalysisRequest(null, null, null, logLevel, days, appName, env));

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatClientRequestSpec).user(promptCaptor.capture());
        String prompt = promptCaptor.getValue();

        // Verify filters are passed to the mock log fetcher
        assertThat(prompt).contains("Fetching logs from 'PROD' environment");
        assertThat(prompt).contains("for the last 3 days");
        assertThat(prompt).contains("for application 'UserService'");
        assertThat(prompt).contains("with level 'ERROR'");
    }

    @Test
    void processLogs_shouldThrowException_whenLogLengthExceedsLimit() {
        // Given
        StringBuilder hugeLog = new StringBuilder();
        for (int i = 0; i < 1_000_001; i++) {
            hugeLog.append("a");
        }
        String rawLogs = hugeLog.toString();

        // When & Then
        assertThatThrownBy(() -> logAnalysisService.processLogs(rawLogs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Raw logs length exceeds the limit");
    }

    @Test
    void processLogs_shouldThrowException_whenQueryLengthExceedsLimit() {
        // Given
        String rawLogs = "logs";
        StringBuilder hugeQuery = new StringBuilder();
        for (int i = 0; i < 100_001; i++) {
            hugeQuery.append("a");
        }
        String query = hugeQuery.toString();

        // When & Then
        assertThatThrownBy(
                        () ->
                                logAnalysisService.processLogs(
                                        new LogAnalysisRequest(
                                                rawLogs, query, null, null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Query length exceeds the limit");
    }
}
