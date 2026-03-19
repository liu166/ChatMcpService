package com.rag.chatmcpservice.service;

import com.rag.chatmcpservice.util.JsonUtil;
import com.rag.chatmcpservice.util.OkHttpUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.zhipuai.ZhiPuAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class McpService {

    @Autowired
    @Lazy
    private ChatClient zhipuChatClient;

    @Autowired
    @Qualifier("zhiPuAiImageModel")
    private ImageModel imageModel;

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
        Map<String, String> map = Map.of("query", query, "fetch_full", "true", "timeout_ms", "30000", "sort", "relevance", "time_range", "day");
        String url = "https://uapis.cn/api/v1/search/aggregate";
        try {
            String message = OkHttpUtil.postJson(url, JsonUtil.toJson(map));
            return message;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Tool(description = """
                根据用户输入的文本描述生成图片
                功能：
                1. 文生图（Text-to-Image）
                2. 返回生成图片的 URL
                输入：
                - query: 用户的文本描述
                输出：
                - 图片 URL 字符串
            """)
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 10000)
    )
    public String generateImage(@ToolParam(description = "用户的文本描述") String query) {
        ZhiPuAiImageOptions build = ZhiPuAiImageOptions.builder().model("cogview-3-flash").build();
        ImagePrompt imagePrompt = new ImagePrompt(query, build);
        ImageResponse call = imageModel.call(imagePrompt);
        String url = call.getResult().getOutput().getUrl();
        return url;
    }

    @Tool(description = """
                根据用户问题分析图片或视频附件，并提取对应内容。
                功能：
                1. 支持图片（jpg/png/gif）和视频（mp4/mov等）文件
                2. 结合用户问题生成每个附件的解析内容
                输入：
                - query: 用户提出的问题（需求）
                - paths: 文件路径列表信息
                输出：
                - String: 每个附件的名称及解析结果文本
            """)
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 10000)
    )
    public String extractMediaContent(
            @ToolParam(description = "用户提出的问题（需求）") String query,
            @ToolParam(description = "文件路径列表信息") List<String> paths) {

        StringBuilder resultBuilder = new StringBuilder();

        for (String path : paths) {
            try {
                File file = new File(path);
                byte[] bytes = Files.readAllBytes(file.toPath());

                // 调用 zhipuChatClient，同步返回 PromptResponse
                String response = zhipuChatClient.prompt()
                        .user(spec -> {
                            spec.text(query);
                            spec.media(MimeTypeUtils.IMAGE_PNG, new ByteArrayResource(bytes));
                        })
                        .call().content(); // 这里直接返回 PromptResponse
                // 汇总每个文件的返回
                resultBuilder.append("【").append(file.getName()).append("】\n");
                resultBuilder.append(response).append("\n\n");

            } catch (IOException e) {
                // 文件读取失败时记录错误
                resultBuilder.append("【").append(path).append("】读取失败: ").append(e.getMessage()).append("\n\n");
            }
        }

        return resultBuilder.toString().trim();
    }
}
