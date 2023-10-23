package com.zeroxn.bbs.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Author: lisang
 * @DateTime: 2023-10-23 13:59:06
 * @Description:
 */
@Component
public class OkHttpClientUtils {
    private static final Logger logger = LoggerFactory.getLogger(OkHttpClientUtils.class);
    private static OkHttpClient client;
    private static ObjectMapper objectMapper;
    @Autowired
    public void setOkHttpclient(OkHttpClient client) {
        OkHttpClientUtils.client = client;
    }
    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        OkHttpClientUtils.objectMapper = objectMapper;
    }

    /**
     *  统一发送请求方法，提供了响应参数校验和凡序列化
     * @param request 需要发送的请求体
     * @param clazz 需要反序列化的类型
     * @return 返回序列化后的对象或空
     * @param <T> 泛型
     */
    public static <T> T sendRequest(Request request, Class<T> clazz) {
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
}
