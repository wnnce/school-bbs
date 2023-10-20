package com.zeroxn.bbs.task.config;

import com.zeroxn.bbs.task.handler.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author: lisang
 * @DateTime: 2023-10-18 21:55:37
 * @Description: 全局Bean配置类
 */
@Configuration
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

}
