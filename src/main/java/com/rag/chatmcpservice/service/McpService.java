package com.rag.chatmcpservice.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rag.chatmcpservice.util.JsonUtil;
import com.rag.chatmcpservice.util.OkHttpUtil;
import kotlin.Result;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class McpService {

    @Tool(description = "获取当前日期时间")
    public String getCurrentDateTime() {
        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"));
        return now;
    }

    @Tool(description = "获取用户输入对应城市的天气详情信息,此工具会返回对应查询城市未来7天（包含当天）的天气详细信息")
    public String weatherInfoByCity(@ToolParam(description = "城市") String city) {
        StringBuffer sb = new StringBuffer();
        sb.append("city=").append(city).append("&extended=true").append("&indices=true").append("&forecast=true");
        String url = "https://uapis.cn/api/v1/misc/weather?" + sb;
        try {
            return OkHttpUtil.get(url);
        } catch (IOException e) {
            try {
                sb = new StringBuffer();
                sb.append("city=").append("深圳").append("&extended=true").append("&indices=true").append("&forecast=true");
                String urlNew = "https://uapis.cn/api/v1/misc/weather?" + sb;
                return OkHttpUtil.get(urlNew);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Tool(
            name = "getRealtimeInformation",
            description = """
                    用于查询当前实时发生的信息，包括但不限于：
                    - 今日股市行情、指数涨跌
                    - 今日黄金、原油、大宗商品价格
                    - 今日热点新闻、社会热点
                    - 今日最新政策、经济动态
                    - 当前发生的重大事件
                    - 今天、现在、最新、当前、最近等实时相关问题
                    
                    适用于任何需要获取最新实时数据的问题。
                    
                    ⚠️ 不用于查询天气信息。
                    天气相关问题必须调用专门的天气查询工具。
                    
                    如果问题涉及实时金融、新闻、市场行情或当天动态，应优先调用本工具。
                    """)
    public String getRealtimeInformation(@ToolParam(description = "用户的问题") String query) {
        Map<String, String> map = Map.of("query", query,  "fetch_full", "true", "timeout_ms", "30000", "sort", "relevance", "time_range", "day");
        String url = "https://uapis.cn/api/v1/search/aggregate";
        try {
            String message = OkHttpUtil.postJson(url, JsonUtil.toJson(map));
            return message;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
