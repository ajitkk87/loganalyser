package com.analyser.loganalyser.model;

public record LogAnalysisRequest(
        String rawLogs,
        String query,
        String repoLink,
        String logLevel,
        Integer days,
        String applicationName,
        String env) {}
