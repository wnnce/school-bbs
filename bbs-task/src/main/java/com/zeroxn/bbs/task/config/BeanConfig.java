package com.zeroxn.bbs.task.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeroxn.bbs.task.handler.hot.*;
import com.zeroxn.bbs.task.handler.review.ImageReviewHandler;
import com.zeroxn.bbs.task.handler.review.ReviewHandler;
import com.zeroxn.bbs.task.handler.review.TextReviewHandler;
import com.zeroxn.bbs.task.handler.review.VideoReviewHandler;
import com.zeroxn.bbs.task.service.impl.TopicReviewService;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-18 21:55:37
 * @Description: 全局Bean配置类
 */
@Configuration(proxyBeanMethods = false)
public class BeanConfig {
    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public HotHandler hotHandler() {
        HotOtherDayHandler otherDayHandler = new HotOtherDayHandler();
        HotFiveDayHandler fiveDayHandler = new HotFiveDayHandler();
        fiveDayHandler.setNextHandler(otherDayHandler);
        HotThereDayHandler thereDayHandler = new HotThereDayHandler();
        thereDayHandler.setNextHandler(fiveDayHandler);
        HotOneDayHandler oneDayHandler = new HotOneDayHandler();
        oneDayHandler.setNextHandler(thereDayHandler);
        return oneDayHandler;
    }
    @Bean
    public ReviewHandler reviewHandler(TopicReviewService reviewService) {
        VideoReviewHandler videoReviewHandler = new VideoReviewHandler(reviewService);
        ImageReviewHandler imageReviewHandler = new ImageReviewHandler(reviewService);
        imageReviewHandler.setNextHandler(videoReviewHandler);
        TextReviewHandler textReviewHandler = new TextReviewHandler(reviewService);
        textReviewHandler.setNextHandler(imageReviewHandler);
        return textReviewHandler;
    }

    /**
     * redis配置 配置自定义的key和value序列化器
     * @param factory redis连接工厂
     * @param objectMapper jackson解析器
     * @return 返回注入的redis模板
     */
    @Bean
    public RedisTemplate<String, List<Integer>> redisTemplate(RedisConnectionFactory factory, ObjectMapper objectMapper) {
        RedisTemplate<String, List<Integer>> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, List.class));
        return redisTemplate;
    }
}
