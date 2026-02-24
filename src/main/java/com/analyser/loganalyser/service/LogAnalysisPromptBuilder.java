package com.analyser.loganalyser.service;

import com.analyser.loganalyser.model.LogAnalysisRequest;
import org.springframework.stereotype.Component;

@Component
public class LogAnalysisPromptBuilder {

    public String buildAnalysisPrompt(LogAnalysisRequest request, String logsToProcess) {
        StringBuilder prompt = new StringBuilder();
        if (request.query() != null && !request.query().isEmpty()) {
            prompt.append(request.query()).append(" ");
        }

        if (request.logLevel() != null
                && !request.logLevel().trim().isEmpty()
                && !"All".equalsIgnoreCase(request.logLevel())) {
            prompt.append("Analyze these logs focusing on ")
                    .append(request.logLevel())
                    .append(" level entries");
        } else {
            prompt.append("Identify errors in these logs");
        }

        if (request.applicationName() != null
                && !request.applicationName().trim().isEmpty()
                && !"All".equalsIgnoreCase(request.applicationName())) {
            prompt.append(" for application ").append(request.applicationName());
        }

        if (request.days() != null) {
            prompt.append(" from the last ").append(request.days()).append(" days");
        }

        if (request.repoLink() != null && !request.repoLink().trim().isEmpty()) {
            prompt.append(". Context repository: ").append(request.repoLink());
        }

        prompt.append(
                "Avoid duplicate errors and Provide the output in a consistent tabular format with the following columns: Exception, Impacted Class, Details of Exception, Remediation of Code.");
        prompt.append(
                " Return only the markdown table and rows. Do not return validation summaries, rule checks, headings, bullet points, or JSON.");
        prompt.append(": ").append(logsToProcess);
        return prompt.toString();
    }

    public String buildEmailPrompt(String emailTemplateContent, String analysis) {
        StringBuilder emailPrompt = new StringBuilder();
        emailPrompt.append("Use the following email template for the alert:\n");
        emailPrompt.append(emailTemplateContent).append("\n\n");
        emailPrompt.append(
                "Fill in the placeholders {applicationName}, {environment}, {date}, and {errorDetails} based on the logs analyzed.\n\n");
        emailPrompt.append("Analysis Result:\n").append(analysis);
        return emailPrompt.toString();
    }
}
