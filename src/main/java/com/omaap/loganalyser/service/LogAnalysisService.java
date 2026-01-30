package com.omaap.loganalyser.service;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
public class LogAnalysisService {

    private final OllamaChatModel chatModel;

    // The starter automatically creates this bean from your yaml settings
    public LogAnalysisService(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String processLogs(String rawLogs) {
        return chatModel.call("Identify errors in these logs: " + rawLogs);
    }
}
