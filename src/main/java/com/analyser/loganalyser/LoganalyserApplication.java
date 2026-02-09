package com.analyser.loganalyser;

import com.analyser.loganalyser.config.LogProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LogProperties.class)
public class LoganalyserApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoganalyserApplication.class, args);
    }
}
