package com.zeroxn.bbs.task;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: lisang
 * @DateTime: 2023-10-18 20:22:55
 * @Description:
 */
@SpringBootApplication
@MapperScan("com.zeroxn.bbs.task.mapper")
public class BbsTaskApplication {
    public static void main(String[] args) {
        SpringApplication.run(BbsTaskApplication.class);
    }
}