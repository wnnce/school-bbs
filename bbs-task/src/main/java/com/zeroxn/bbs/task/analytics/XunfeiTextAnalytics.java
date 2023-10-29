package com.zeroxn.bbs.task.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeroxn.bbs.task.config.xunfei.XunfeiAnalyticsProperties;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: lisang
 * @DateTime: 2023-10-19 11:01:28
 * @Description: 基于讯飞平台的文本关键字提取
 */
public class XunfeiTextAnalytics implements TextAnalytics{
    private static final Logger logger = LoggerFactory.getLogger(XunfeiTextAnalytics.class);
    private static final Map<String, CacheItem> cacheMap = new HashMap<>();
    private final XunfeiAnalyticsProperties properties;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    public XunfeiTextAnalytics(XunfeiAnalyticsProperties properties, OkHttpClient client, ObjectMapper objectMapper) {
        this.properties = properties;
        this.client = client;
        this.objectMapper = objectMapper;
    }

    /**
     * 提取字符串中的关键字
     * @param text 需要提取关键字的字符串
     * @param size 提取的关键字个数
     * @return 返回提取出的关键字
     */
    @Override
    public List<String> keywordsExtraction(String text, int size) {
        RequestBody requestBody = new FormBody.Builder()
                .add("text", text)
                .build();
        Request request = makeRequest().post(requestBody).build();
        try{
            Response response = client.newCall(request).execute();
            String resultString = response.body().string();
            XunfeiResult xunfeiResult = stringToObject(resultString, XunfeiResult.class);
            if ("0".equals(xunfeiResult.code) && xunfeiResult.data() != null) {
                List<Keyword> keywordList = xunfeiResult.data().get("ke");
                if (keywordList.size() <= size) {
                    return keywordList.stream().map(Keyword::word).toList();
                }else {
                    return keywordList.subList(0, size).stream().map(Keyword::word).toList();
                }
            }else {
                logger.error("讯飞接口响应码错误，响应内容：{}", xunfeiResult);
            }

        }catch (IOException ex) {
           logger.error("发送请求失败，错误信息：{}", ex.getMessage());
        }
        return null;
    }

    private Request.Builder makeRequest() {
        Long currentTime = System.currentTimeMillis() / 1000;
        String xParam = initXParam();
        return new Request.Builder()
                .url(properties.getRequestUrl())
                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .header("X-Appid", properties.getAppid())
                .header("X-CurTime", currentTime.toString())
                .header("X-Param", xParam)
                .header("X-CheckSum", makeCheckSum(currentTime, xParam));
    }

    private String initXParam() {
        CacheItem param = cacheMap.get("param");
        if (param == null) {
            Type type = new Type(properties.getType());
            String typeJson = objectToString(type);
            typeJson = Base64.getEncoder().encodeToString(typeJson.getBytes(StandardCharsets.UTF_8));
            param = new CacheItem(typeJson, null);
            cacheMap.put("param", param);
        }
        return param.data();
    }

    private String makeCheckSum(Long currentTime, String xParam) {
        CacheItem checkSum = cacheMap.get("checkSum");
        if (checkSum == null || checkSum.expire() > System.currentTimeMillis() / 1000) {
            String originValue = properties.getApiKey() + currentTime + xParam;
            try{
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] md5 = md.digest(originValue.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : md5) {
                    sb.append(String.format("%02x", b & 0xff));
                }
                checkSum = new CacheItem(sb.toString(), Duration.ofMinutes(3).toSeconds() + currentTime);
                cacheMap.put("checkSum", checkSum);
            }catch (NoSuchAlgorithmException ex) {
                logger.error("请求参数md5摘要失败，错误信息：{}", ex.getMessage());
                return "";
            }
        }
        return checkSum.data();
    }
    private <T> T stringToObject(String value, Class<T> clazz) {
        T object = null;
        try {
            object = objectMapper.readValue(value, clazz);
        }catch (JsonProcessingException ex) {
            logger.error("Json发序列化失败，错误信息:{}", ex.getMessage());
        }
        return object;
    }

    private String objectToString(Object object) {
        String value = null;
        try {
            value = objectMapper.writeValueAsString(object);
        }catch (JsonProcessingException ex) {
            logger.error("Json序列化失败，错误信息：{}", ex.getMessage());
        }
        return value;
    }
    private record Type(String type) {}
    public record CacheItem(String data, Long expire) {}
    public record XunfeiResult(String code, String desc, String sid, Map<String, List<Keyword>> data) {}
    public record Keyword(Float score, String word) {}
}
