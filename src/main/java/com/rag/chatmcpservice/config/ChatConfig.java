package com.rag.chatmcpservice.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Bean("zhipu")
    public ChatClient zhipuChatClient(ZhiPuAiChatModel model) {
        return ChatClient.create(model);
    }
}
