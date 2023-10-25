package com.zeroxn.bbs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Author: lisang
 * @DateTime: 2023-10-10 20:48:07
 * @Description: 论坛程序启动类
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.zeroxn.bbs.web.mapper")
public class ForumApplication {
    public static void main(String[] args) {
        SpringApplication.run(ForumApplication.class, args);
    }
}