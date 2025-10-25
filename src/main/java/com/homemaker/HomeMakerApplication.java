package com.homemaker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 家政服务平台启动类
 */
@SpringBootApplication
@MapperScan("com.homemaker.mapper")
public class HomeMakerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeMakerApplication.class, args);
    }

}