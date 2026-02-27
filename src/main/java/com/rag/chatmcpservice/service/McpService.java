package com.rag.chatmcpservice.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class McpService {

    @Tool(description = "获取当前日期时间")
    public String getCurrentDateTime() {
        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"));
        return now;
    }


}
