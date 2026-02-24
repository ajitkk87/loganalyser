package com.analyser.loganalyser.service;

@FunctionalInterface
public interface LogFetcher {
    String fetchLogs(String env, Integer days, String logLevel, String applicationName);
}
