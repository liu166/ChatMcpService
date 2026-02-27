package com.rag.chatmcpservice.util;

import okhttp3.*;
import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OkHttpUtil {

    // 静态单例 OkHttpClient（线程安全，可高并发）
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(200, 5, TimeUnit.MINUTES))
            .retryOnConnectionFailure(true)
            .build();

    private OkHttpUtil() {
        // 私有构造，防止实例化
    }

    /** GET 请求 */
    public static String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    public static String postJson(String url, String json) throws IOException {
        return postJson(url, json, null);
    }

    /** POST JSON */
    public static String postJson(String url, String json, String token) throws IOException {
        RequestBody body = RequestBody.create(
                json,
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );
        // 构建请求，添加 token 到 Authorization header
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json");

        // 如果 token 不为空，添加到请求头
        if (token != null && !token.isEmpty()) {
            // 情况1：Bearer token（最常见）
            requestBuilder.addHeader("Authorization", "Bearer " + token);

        }
        Request request = requestBuilder.build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // 增加更详细的错误信息
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("Unexpected code " + response.code() +
                        ", body: " + errorBody +
                        ", url: " + url);
            }
            return response.body().string();
        }
    }

    /** POST 表单 */
    public static String postForm(String url, FormBody formBody) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }
}