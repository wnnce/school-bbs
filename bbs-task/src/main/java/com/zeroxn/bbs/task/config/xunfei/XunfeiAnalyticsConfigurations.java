package com.zeroxn.bbs.task.config.xunfei;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.zeroxn.bbs.task.analytics.TextAnalytics;
import com.zeroxn.bbs.task.analytics.XunfeiTextAnalytics;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author: lisang
 * @DateTime: 2023-10-19 16:56:32
 * @Description:
 */
public class XunfeiAnalyticsConfigurations {
    static class ClientConfiguration {
        @Bean
         OkHttpClient okHttpClient() {
            return new OkHttpClient.Builder()
                    .readTimeout(Duration.ofSeconds(10))
                    .writeTimeout(Duration.ofSeconds(10))
                    .callTimeout(Duration.ofSeconds(10))
                    .build();
        }
    }
    static class AnalyticsConfiguration {
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            simpleModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
            simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            simpleModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
            objectMapper.registerModule(simpleModule);

            //不序列化null值
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return objectMapper;
        }
        @Bean
        TextAnalytics xunfeiTextAnalytics(OkHttpClient client, ObjectMapper objectMapper, XunfeiAnalyticsProperties properties) {
            return new XunfeiTextAnalytics(properties, client, objectMapper);
        }
    }
}
