package com.rag.chatmcpservice.service.config;

import com.rag.chatmcpservice.service.McpService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolsConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(McpService mcpService) {
        return MethodToolCallbackProvider.builder().toolObjects(mcpService).build();
    }
}
