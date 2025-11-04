package com.homemaker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 家政服务平台启动类
 */
@SpringBootApplication
@MapperScan("com.homemaker.mapper")
public class HomeMakerApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(HomeMakerApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(HomeMakerApplication.class, args);
    }

}