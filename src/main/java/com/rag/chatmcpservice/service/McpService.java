package com.rag.chatmcpservice.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rag.chatmcpservice.util.OkHttpUtil;
import kotlin.Result;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
public class McpService {

    @Tool(description = "获取当前日期时间")
    public String getCurrentDateTime() {
        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"));
        return now;
    }

    @Tool(description = "获取用户输入对应城市的天气详情信息")
    public String weatherInfoByCity(@ToolParam(description = "城市") String city) {
        StringBuffer sb = new StringBuffer();
        sb.append("city=").append(city).append("&extended=true").append("&indices=true").append("&forecast=true");
        String url = "https://uapis.cn/api/v1/misc/weather?"+ sb;
        try {
            return OkHttpUtil.get(url);
        } catch (IOException e) {
            try {
                sb = new StringBuffer();
                sb.append("city=").append("深圳").append("&extended=true").append("&indices=true").append("&forecast=true");
                String urlNew = "https://uapis.cn/api/v1/misc/weather?"+ sb;
                return OkHttpUtil.get(urlNew);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
