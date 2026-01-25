package org.example.synjiserver.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.example.synjiserver.service.ScheduleExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    @Bean
    public ChatLanguageModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(60))
                .temperature(0.1) // 降低随机性，减少幻觉和重复
                .maxTokens(2000)  // 增加最大 Token 数，防止 JSON 截断
                .build();
    }

    @Bean
    public ScheduleExtractor scheduleExtractor(ChatLanguageModel chatModel) {
        return AiServices.builder(ScheduleExtractor.class)
                .chatLanguageModel(chatModel)
                .build();
    }
}
