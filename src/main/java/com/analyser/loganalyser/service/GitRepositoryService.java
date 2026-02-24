package com.analyser.loganalyser.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GitRepositoryService {

    private static final Duration CLONE_TIMEOUT = Duration.ofMinutes(2);
    private final Path repoBaseDir;

    public GitRepositoryService(
            @Value("${app.repo-clone.base-dir:output/cloned-repos}") String repoBaseDir) {
        this.repoBaseDir = Paths.get(repoBaseDir).toAbsolutePath().normalize();
    }

    public String cloneRepositoryIfApplicable(String repoLink) {
        if (repoLink == null || repoLink.trim().isEmpty()) {
            return repoLink;
        }
        if (!looksLikeGitRepoLink(repoLink)) {
            return repoLink;
        }

        Path cloneDir = buildClonePath(repoLink.trim());
        try {
            Files.createDirectories(repoBaseDir);
            Process process =
                    new ProcessBuilder(
                                    "git",
                                    "clone",
                                    "--depth",
                                    "1",
                                    repoLink.trim(),
                                    cloneDir.toString())
                            .redirectErrorStream(true)
                            .start();
            boolean finished = process.waitFor(CLONE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalArgumentException(
                        "Git clone timed out after " + CLONE_TIMEOUT.toSeconds() + " seconds.");
            }
            if (process.exitValue() != 0) {
                throw new IllegalArgumentException(
                        "Git clone failed for repo link: " + repoLink.trim());
            }
            return repoLink.trim() + " (cloned to " + cloneDir.toAbsolutePath() + ")";
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Unable to run git clone. Ensure git is installed and accessible.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Git clone was interrupted.", e);
        }
    }

    private boolean looksLikeGitRepoLink(String repoLink) {
        String normalized = repoLink.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("https://")
                || normalized.startsWith("http://")
                || normalized.startsWith("ssh://")
                || normalized.startsWith("git@");
    }

    private Path buildClonePath(String repoLink) {
        String folderName = sanitizeRepoFolderName(repoLink) + "-" + Instant.now().toEpochMilli();
        return repoBaseDir.resolve(folderName);
    }

    private String sanitizeRepoFolderName(String repoLink) {
        String[] parts = repoLink.replace('\\', '/').split("/");
        String candidate = parts.length == 0 ? "repo" : parts[parts.length - 1];
        if (candidate.endsWith(".git")) {
            candidate = candidate.substring(0, candidate.length() - 4);
        }
        String sanitized = candidate.replaceAll("[^a-zA-Z0-9._-]", "_");
        return sanitized.isBlank() ? "repo" : sanitized;
    }
}
