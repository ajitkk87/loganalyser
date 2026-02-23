package com.analyser.loganalyser.model.agent;

import java.util.List;

public record AgentCard(
        String id,
        String name,
        String description,
        String version,
        List<String> capabilities,
        String invokeEndpoint) {}
