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
    private static final String AUTH_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String TEXT_URL = "https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined";
    private static final String IMAGE_URL = "https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined";
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
