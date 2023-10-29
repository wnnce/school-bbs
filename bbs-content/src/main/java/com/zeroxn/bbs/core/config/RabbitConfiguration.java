package com.zeroxn.bbs.core.config;

import com.zeroxn.bbs.base.constant.QueueConstant;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;

/**
 * @Author: lisang
 * @DateTime: 2023-10-28 21:42:04
 * @Description: RabbitMQ配置类，类被初始化时自动创建需要使用的Queue
 */
@Configuration(proxyBeanMethods = false)
public class RabbitConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(RabbitConfiguration.class);
    private final AmqpAdmin amqpAdmin;

    public RabbitConfiguration(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    /**
     * 自定义消息队列的消息解析器，注入jackson的消息解析器，消息序列化为Json数据，默认为base64
     * @return 返回基于Jackson的消息解析器
     */
    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @PostConstruct
    public void init() throws Exception {
        Class<QueueConstant> queuesClass = QueueConstant.class;
        Field[] fields = queuesClass.getFields();
        logger.info("待注入的Queue数量：{}", fields.length);
        for (Field field : fields) {
            String queueName = (String) field.get(null);
            Queue queue = new Queue(queueName, true);
            logger.info("添加 {} Queue", queueName);
            amqpAdmin.declareQueue(queue);
        }
    }

}