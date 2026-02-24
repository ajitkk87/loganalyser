package com.analyser.loganalyser.service;

import com.analyser.loganalyser.model.LogAnalysisRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class LogAnalysisService {

    private static final int MAX_LOG_LENGTH = 1_000_000;
    private static final int MAX_QUERY_LENGTH = 100_000;
    private final LogAnalysisPromptBuilder promptBuilder;
    private final LogFetcher logFetcher;
    private final GitRepositoryService gitRepositoryService;
    private final EmailAlertService emailAlertService;
    private final AnalysisOutputStore analysisOutputStore;
    private final PromptTemplateService promptTemplateService;
    private final ChatClient chatClient;

    public LogAnalysisService(
            ChatClient chatClient,
            LogAnalysisPromptBuilder promptBuilder,
            LogFetcher logFetcher,
            GitRepositoryService gitRepositoryService,
            EmailAlertService emailAlertService,
            AnalysisOutputStore analysisOutputStore,
            PromptTemplateService promptTemplateService) {
        this.chatClient = chatClient;
        this.promptBuilder = promptBuilder;
        this.logFetcher = logFetcher;
        this.gitRepositoryService = gitRepositoryService;
        this.emailAlertService = emailAlertService;
        this.analysisOutputStore = analysisOutputStore;
        this.promptTemplateService = promptTemplateService;
    }

    public String processLogs(String rawLogs) {
        return processLogs(new LogAnalysisRequest(rawLogs, null, null, null, null, null, null));
    }

    public String processLogs(LogAnalysisRequest request) {
        String logsToProcess = request.rawLogs();
        if (request.env() != null && !request.env().trim().isEmpty()) {
            logsToProcess =
                    logFetcher.fetchLogs(
                            request.env(),
                            request.days(),
                            request.logLevel(),
                            request.applicationName());
        }
        if (logsToProcess == null || logsToProcess.isBlank()) {
            throw new IllegalArgumentException("No logs available to analyze.");
        }

        if (logsToProcess.length() > MAX_LOG_LENGTH) {
            throw new IllegalArgumentException(
                    "Raw logs length exceeds the limit of " + MAX_LOG_LENGTH + " characters.");
        }

        if (request.query() != null && request.query().length() > MAX_QUERY_LENGTH) {
            throw new IllegalArgumentException(
                    "Query length exceeds the limit of " + MAX_QUERY_LENGTH + " characters.");
        }

        String repoContext = gitRepositoryService.cloneRepositoryIfApplicable(request.repoLink());
        LogAnalysisRequest promptRequest =
                new LogAnalysisRequest(
                        request.rawLogs(),
                        request.query(),
                        repoContext,
                        request.logLevel(),
                        request.days(),
                        request.applicationName(),
                        request.env());

        String guardrails = promptTemplateService.guardrailsTemplate();
        String prompt = promptBuilder.buildAnalysisPrompt(promptRequest, logsToProcess);
        String result =
                (guardrails != null && !guardrails.isBlank())
                        ? chatClient.prompt().system(guardrails).user(prompt).call().content()
                        : chatClient.prompt().user(prompt).call().content();

        analysisOutputStore.save(result);

        if (result != null && (result.contains("ERROR") || result.contains("Exception"))) {
            String emailPrompt =
                    promptBuilder.buildEmailPrompt(promptTemplateService.emailTemplate(), result);
            String emailResult =
                    (guardrails != null && !guardrails.isBlank())
                            ? chatClient
                                    .prompt()
                                    .system(guardrails)
                                    .user(emailPrompt)
                                    .call()
                                    .content()
                            : chatClient.prompt().user(emailPrompt).call().content();
            emailAlertService.sendEmailAlert("Log Analysis Error Alert", emailResult);
        }
        return result;
    }
}
