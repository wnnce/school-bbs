package com.zeroxn.bbs.core.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeroxn.bbs.core.cache.CacheService;
import com.zeroxn.bbs.core.config.wechat.WechatProperties;
import com.zeroxn.bbs.core.exception.ExceptionUtils;
import lombok.Getter;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 18:43:02
 * @Description: 微信接口服务层
 */

@Getter
public class WechatService {
    private static final Logger logger = LoggerFactory.getLogger(WechatService.class);

    private final WechatProperties properties;

    private final OkHttpClient client;

    private final ObjectMapper objectMapper;

    private final CacheService cacheService;

    public WechatService(WechatProperties properties, OkHttpClient client, ObjectMapper objectMapper, CacheService cacheService) {
        this.properties = properties;
        this.client = client;
        this.objectMapper = objectMapper;
        this.cacheService = cacheService;
    }

    /**
     * 获取微信的AccessToken 获取成功后放到缓存里面。AccessToken2个小时内有效，缓存设置为110分钟过期
     * @return 返回获取的AccessToken
     */
    private String getAccessToken() {
        String accessToken = cacheService.getCache("wechatAccessToken", String.class);
        if (accessToken != null && !accessToken.isEmpty()) {
            return accessToken;
        }
        HttpUrl tokenUrl = HttpUrl.get(properties.getTokenUrl()).newBuilder()
                .addQueryParameter("appid", properties.getAppid())
                .addQueryParameter("secret", properties.getSecret())
                .addQueryParameter("grant_type", properties.getTokenGrantType())
                .build();
        Request request = new Request.Builder()
                .url(tokenUrl.url())
                .build();
        TokenResult result = this.sendRequest(request, TokenResult.class);
        if (result != null) {
            if (result.access_token() != null && !result.access_token().isEmpty()) {
                accessToken = result.access_token();
                cacheService.setCache("wechatAccessToken", accessToken, Duration.ofMinutes(110));
                return accessToken;
            }
        }
        return null;
    }

    /**
     * 获取用户的openId
     * @param code 前端拿到的微信登录Code
     * @return 返回用户的openId
     */
    public String getOpenId(String code) {
        HttpUrl loginUrl = HttpUrl.get(properties.getLoginUrl()).newBuilder()
                .addQueryParameter("appid", properties.getAppid())
                .addQueryParameter("secret", properties.getSecret())
                .addQueryParameter("js_code", code)
                .addQueryParameter("grant_type", properties.getLoginGrantType())
                .build();
        Request request = new Request.Builder().url(loginUrl.url()).build();
        LoginResult result = this.sendRequest(request, LoginResult.class);
        if (result != null) {
            if (result.openid() != null && !result.openid().isEmpty()){
                return result.openid();
            }
        }
        return null;
    }

    /**
     * 获取用户手机号
     * @param code 前端调用微信手机号接口拿到的code
     * @return 返回手机号
     */
    public String getPhone(String code) {
        String accessToken = this.getAccessToken();
        ExceptionUtils.isConditionThrowServer(accessToken == null, "获取手机号失败");
        HttpUrl phoneUrl = HttpUrl.get(properties.getPhoneUrl()).newBuilder()
                .addQueryParameter("access_token", accessToken)
                .addQueryParameter("code", code)
                .build();
        Request request = new Request.Builder()
                .url(phoneUrl.url())
                .method("post", null)
                .build();
        PhoneResult result = this.sendRequest(request, PhoneResult.class);
        if (result != null) {
            if (result.phone_info().phoneNumber != null && !result.phone_info().phoneNumber().isEmpty()) {
                return result.phone_info().phoneNumber();
            }
        }
        return null;
    }

    /**
     * 统一发送请求方法，负责请求发送和响应数据的反序列化
     * @param request 需要发送的请求
     * @param clazz 需要反序列化的类型
     * @return 返回响应参数反序列后的对象
     * @param <T> 泛型
     */
    private <T> T sendRequest(Request request, Class<T> clazz) {
        try{
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String body = response.body().string();
                return objectMapper.readValue(body, clazz);
            }
        }catch (IOException ex) {
            logger.error("请求失败，错误信息：{}", ex.getMessage());
        }
        return null;
    }

    // 临时数据封装对象
    private record LoginResult(String openid, String unionid, Integer errcode, String errmsg, String session_key) {}
    private record TokenResult(String access_token, Integer expires_in) {}
    private record PhoneResult(Integer errcode, String errmsg, PhoneInfo phone_info) {}
    private record PhoneInfo(String phoneNumber, String purePhoneNumber, Integer countryCode, Watermark watermark) {}
    private record Watermark(Long timestamp, String appid) {}
}
