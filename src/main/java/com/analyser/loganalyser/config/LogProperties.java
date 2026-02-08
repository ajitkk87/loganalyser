package com.analyser.loganalyser.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;

@ConfigurationProperties(prefix = "log")
public class LogProperties {

    private Map<String, String> envUrls;

    public Map<String, String> getEnvUrls() {
        return envUrls;
    }

    public void setEnvUrls(Map<String, String> envUrls) {
        this.envUrls = envUrls;
    }
}