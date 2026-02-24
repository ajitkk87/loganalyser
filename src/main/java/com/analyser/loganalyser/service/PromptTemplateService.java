package com.analyser.loganalyser.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PromptTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateService.class);
    private static final String GUARDRAILS_FILE = "templates/guardrails.st";
    private static final String EMAIL_TEMPLATE_FILE = "templates/email-alert.st";

    private final String guardrailsTemplate;
    private final String emailTemplate;

    public PromptTemplateService() {
        this.guardrailsTemplate = loadTemplate(GUARDRAILS_FILE);
        this.emailTemplate = loadTemplate(EMAIL_TEMPLATE_FILE);
    }

    public String guardrailsTemplate() {
        return guardrailsTemplate;
    }

    public String emailTemplate() {
        return emailTemplate;
    }

    private String loadTemplate(String classpathFile) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathFile);
            try (InputStream in = resource.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not load template {}: {}", classpathFile, e.getMessage());
            }
            return "";
        }
    }
}
