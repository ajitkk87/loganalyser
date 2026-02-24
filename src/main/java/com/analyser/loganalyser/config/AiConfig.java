package com.analyser.loganalyser.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;

@Configuration
public class AiConfig {
    private static final Logger logger = LoggerFactory.getLogger(AiConfig.class);

    /**
     * Primary bean selector for a {@link ChatModel} implementation.
     *
     * <p>This bean picks the chat provider implementation based on the {@code ai.provider} property
     * (defaults to {@code ollama}). It uses {@link ObjectProvider} for each supported provider to
     * avoid eager initialization and to allow the application to start when only one provider's
     * starter is present on the classpath.
     *
     * <p>Selection logic:
     *
     * <ol>
     *   <li>If {@code ai.provider=google} and Google GenAI is available, return that.
     *   <li>If {@code ai.provider=ollama} and Ollama is available, return that.
     *   <li>If {@code ai.provider=openai} and OpenAI is available, return that.
     *   <li>If {@code ai.provider} has an unsupported value, throw {@link IllegalStateException}.
     *   <li>If the selected provider is unavailable or unconfigured, throw {@link
     *       IllegalStateException}.
     * </ol>
     *
     * @param env the Spring Environment used to read {@code ai.provider}
     * @param googleProvider optional provider for {@link GoogleGenAiChatModel}
     * @param ollamaProvider optional provider for {@link
     *     org.springframework.ai.ollama.OllamaChatModel}
     * @param openaiProvider optional provider for {@link OpenAiChatModel}
     * @return the selected {@link ChatModel} implementation
     * @throws IllegalStateException if provider is invalid or unavailable
     */
    @Bean
    @Primary
    public ChatModel chatModel(
            Environment env,
            ObjectProvider<GoogleGenAiChatModel> googleProvider,
            ObjectProvider<org.springframework.ai.ollama.OllamaChatModel> ollamaProvider,
            ObjectProvider<OpenAiChatModel> openaiProvider) {
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
        } else if ("openai".equalsIgnoreCase(providerPref)) {
            OpenAiChatModel oa = openaiProvider.getIfAvailable();
            if (oa != null) {
                return oa;
            }
        } else {
            throw new IllegalStateException(
                    "Invalid ai.provider: '"
                            + providerPref
                            + "'. Supported values: 'google', 'ollama', 'openai'");
        }

        throw new IllegalStateException(
                "ChatModel '"
                        + providerPref
                        + "' is not available. Ensure the corresponding starter is on the classpath and properties are configured.");
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
    public OpenAiApi openAiApi(Environment env) {
        String apiKey = env.getProperty("spring.ai.openai.api-key");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = env.getProperty("OPENAI_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = env.getProperty("OPEN_API_KEY");
        }

        String baseUrl = env.getProperty("spring.ai.openai.base-url", "https://api.openai.com");
        String completionsPath =
                env.getProperty("spring.ai.openai.chat.completions-path", "/v1/chat/completions");
        String embeddingsPath =
                env.getProperty("spring.ai.openai.embedding.embeddings-path", "/v1/embeddings");

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);

        logger.info("Configuring OpenAiApi with Base URL: {}", baseUrl);
        logger.info("Configuring OpenAiApi with Embeddings Path: {}", embeddingsPath);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("OpenAI API Key is missing");
        }

        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .headers(headers)
                .completionsPath(completionsPath)
                .embeddingsPath(embeddingsPath)
                .build();
    }
}
