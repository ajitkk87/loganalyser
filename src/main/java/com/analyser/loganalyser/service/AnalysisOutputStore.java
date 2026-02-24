package com.analyser.loganalyser.service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AnalysisOutputStore {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisOutputStore.class);
    private static final String LOG_OUTPUT_DIR = "output/log_analysis_output";

    public void save(String output) {
        try {
            Files.createDirectories(Paths.get(LOG_OUTPUT_DIR));
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");
            String fileName =
                    String.format("%s/analysis_%s.txt", LOG_OUTPUT_DIR, now.format(formatter));

            try (FileWriter writer = new FileWriter(fileName, StandardCharsets.UTF_8)) {
                writer.write("Log Analysis Output\n");
                writer.write("Generated at: " + now + "\n");
                writer.write("=".repeat(80) + "\n\n");
                writer.write(output);
            }
            logger.info("Analysis output saved to: {}", fileName);
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error saving output to file: {}", e.getMessage(), e);
            }
        }
    }
}
