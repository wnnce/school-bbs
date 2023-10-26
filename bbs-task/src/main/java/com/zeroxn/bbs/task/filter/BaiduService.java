package com.zeroxn.bbs.task.filter;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeroxn.bbs.base.cache.CacheService;
import com.zeroxn.bbs.task.config.baidu.BaiduProperties;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-23 13:44:56
 * @Description: 百度智能云服务层
 */
public class BaiduService {
    private static final Logger logger = LoggerFactory.getLogger(BaiduService.class);
    /**
     * 获取AccessToken的网址
     */
    private static final String AUTH_URL = "https://aip.baidubce.com/oauth/2.0/token";
    /**
     * 文本审核地址
     */
    private static final String TEXT_URL = "https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined";
    /**
     * 图像审核地址
     */
    private static final String IMAGE_URL = "https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined";
    /**
     * 视频审核地址
     */
    private static final String VIDEO_URL = "https://aip.baidubce.com/rest/2.0/solution/v1/video_censor/v2/user_defined";
    private final BaiduProperties properties;
    private final CacheService cacheService;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    public BaiduService(BaiduProperties properties, CacheService cacheService, OkHttpClient client, ObjectMapper objectMapper) {
        this.properties = properties;
        this.cacheService = cacheService;
        this.client = client;
        this.objectMapper = objectMapper;
    }

    /**
     * 文本内容审查 直接返回封装后的百度接口返回参数
     * @param text 需要审查的字符串
     * @return 返回封装后请求返回参数
     */
    public ReviewResult textReview(String text) {
        String accessToken = makeAccessToken();
        if (accessToken == null) {
            logger.error("获取百度accessToken失败");
            return null;
        }
        HttpUrl httpUrl = HttpUrl.get(TEXT_URL).newBuilder()
                .addQueryParameter("access_token", accessToken)
                .build();
        RequestBody requestBody = new FormBody.Builder()
                .add("text", text)
                .build();
        Request request = new Request.Builder()
                .url(httpUrl.url())
                .post(requestBody)
                .build();
        return this.sendRequest(request, ReviewResult.class);
    }

    /**
     * 图像内容审查 返回响应参数封装的JsonNode对象
     * @param imageUrl 需要审查的图像地址
     * @return 返回封装响应参数的JsonNode
     */
    public JsonNode imageReview(String imageUrl) {
        String accessToken = makeAccessToken();
        if (accessToken == null) {
            logger.error("获取百度accessToken失败");
            return null;
        }
        HttpUrl httpUrl = HttpUrl.get(IMAGE_URL).newBuilder()
                .addQueryParameter("access_token", accessToken)
                .build();
        RequestBody requestBody = new FormBody.Builder()
                .add("imgUrl", imageUrl)
                .add("imgType", "0")
                .build();
        Request request = new Request.Builder()
                .url(httpUrl.url())
                .post(requestBody)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .build();
        return this.sendRequest(request, JsonNode.class);
    }

    /**
     * 视频内容审查
     * @param taskId 本次审查任务的Id
     * @param videoName 审查视频的名称
     * @param videoUrl 视频的地址链接
     * @return 返回封装为JsonNode 的响应参数
     */
    public JsonNode videoReview(String taskId, String videoName, String videoUrl) {
        String accessToken = makeAccessToken();
        if (accessToken == null) {
            logger.error("获取百度accessToken失败");
            return null;
        }
        HttpUrl httpUrl = HttpUrl.get(VIDEO_URL).newBuilder()
                .addQueryParameter("access_token", accessToken)
                .build();
        RequestBody requestBody = new FormBody.Builder()
                .add("name", videoName)
                .add("videoUrl", videoUrl)
                .add("extId", taskId)
                .build();
        Request request = new Request.Builder()
                .url(httpUrl.url())
                .post(requestBody)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        return this.sendRequest(request, JsonNode.class);
    }

    /**
     * 私有参数 获取百度平台的AccessToken，会先尝试从缓存中获取Token，如果缓存中不存在再从百度接口获取，
     * Token有效期30天成功获取后需添加到缓存
     * @return 返回获取到的Token
     */
    private String makeAccessToken() {
        String accessToken = cacheService.getCache("baidu_access_token", String.class);
        if (accessToken == null) {
            HttpUrl httpUrl = HttpUrl.get(AUTH_URL).newBuilder()
                    .addQueryParameter("grant_type", properties.getGrantType())
                    .addQueryParameter("client_id", properties.getAppKey())
                    .addQueryParameter("client_secret", properties.getSecretKey())
                    .build();
            Request request = new Request.Builder()
                    .url(httpUrl.url())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            AuthResult authResult = this.sendRequest(request, AuthResult.class);
            if (authResult == null) {
                return null;
            }
            accessToken = authResult.access_token();
            cacheService.setCache("baidu_access_token", accessToken, Duration.ofSeconds(authResult.expires_in()));
        }
        return accessToken;
    }

    /**
     * 私有方法 统一client请求方法，同时提供响应参数的反序列化封装
     * @param request 需要发送的请求
     * @param clazz 需要反序列化成成的类
     * @return 返回反序列化后的对象或空
     * @param <T> 泛型
     */
    public <T> T sendRequest(Request request, Class<T> clazz) {
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String body = response.body().string();
                return objectMapper.readValue(body, clazz);
            }else {
                logger.error("OkHttp请求失败，错误码：{}，错误消息：{}", response.code(), response.message());
            }
        }catch (IOException e) {
            logger.error("OkHttp发送请求异常，错误信息：{}", e.getMessage());
        }
        return null;
    }

    public record ReviewResult(Long log_id, String conclusion, Integer conclusionType, List<ResultData> data) {}
    public record ResultData(Integer type, Integer subType, String conclusion, List<String> codes, Integer conclusionType, String msg, List<DataHit> hits) {}
    public record DataHit(List<Object> modelHitPositions, List<Object> wordHitPositions, String probability, String datasetName, List<String> words) {}
    private record AuthResult(String refresh_token, Long expires_in, String session_key, String session_secret,
                              String access_token, String scope) {}
}
