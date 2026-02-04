package com.analyser.loganalyser.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;

@Configuration
public class AiConfig {

    /**
     * Primary bean selector for a {@link ChatModel} implementation.
     *
     * <p>This bean picks the chat provider implementation based on the
     * {@code ai.provider} property (defaults to {@code ollama}). It uses
     * {@link ObjectProvider} for each supported provider to avoid eager
     * initialization and to allow the application to start when only one
     * provider's starter is present on the classpath.
     *
     * Selection logic:
     * <ol>
     *   <li>If {@code ai.provider=google} and Google GenAI is available, return that.</li>
     *   <li>If {@code ai.provider=ollama} and Ollama is available, return that.</li>
     *   <li>If {@code ai.provider} has an unsupported value, throw {@link IllegalStateException}.</li>
     *   <li>If the selected provider is unavailable or unconfigured, throw {@link IllegalStateException}.</li>
     * </ol>
     *
     * @param env the Spring Environment used to read {@code ai.provider}
     * @param googleProvider optional provider for {@link GoogleGenAiChatModel}
     * @param ollamaProvider optional provider for {@link org.springframework.ai.ollama.OllamaChatModel}
     * @return the selected {@link ChatModel} implementation
     * @throws IllegalStateException if provider is invalid or unavailable
     */
    @Bean
    @Primary
    public ChatModel chatModel(Environment env,
                              ObjectProvider<GoogleGenAiChatModel> googleProvider,
                              ObjectProvider<org.springframework.ai.ollama.OllamaChatModel> ollamaProvider) {
        String providerPref = env.getProperty("ai.provider", "ollama");

        if ("google".equalsIgnoreCase(providerPref)) {
            GoogleGenAiChatModel g = googleProvider.getIfAvailable();
            if (g != null) {
                return g;
            }
        } else if ("ollama".equalsIgnoreCase(providerPref)) {
            org.springframework.ai.ollama.OllamaChatModel o = ollamaProvider.getIfAvailable();
            if (o != null) {
                return o;
            }
        } else {
            throw new IllegalStateException("Invalid ai.provider: '" + providerPref + "'. Supported values: 'google', 'ollama'");
        }

        throw new IllegalStateException("ChatModel '" + providerPref + "' is not available. Ensure the corresponding starter is on the classpath and properties are configured.");
    }
}
