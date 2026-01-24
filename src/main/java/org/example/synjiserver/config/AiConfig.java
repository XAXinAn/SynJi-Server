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
                // 强制让模型返回 JSON 格式（如果模型支持 response_format 参数，这里可以配置，
                // 但 LangChain4j 的 AiServices 配合 POJO 返回值通常会自动处理）
                .build();
    }

    @Bean
    public ScheduleExtractor scheduleExtractor(ChatLanguageModel chatModel) {
        return AiServices.builder(ScheduleExtractor.class)
                .chatLanguageModel(chatModel)
                .build();
    }
}
