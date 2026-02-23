package com.analyser.loganalyser.model.agent;

public record AgentInvokeRequest(
        String logs,
        String query,
        String repoLink,
        String logLevel,
        Integer days,
        String applicationName,
        String environment) {}
