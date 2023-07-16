package com.zucchini.zucchini_back.global.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer {

    // CORS 관련 처리
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // CORS 적용할 url 패턴 정의
        // /** -> 모든 url에 대한 접근 허용
        registry.addMapping("/**")
                // protocol + host + port -> Origin
                .allowedOrigins("http://localhost:8080")
                // 허용하고자 하는 Http Method
                .allowedMethods(
                        "GET", "POST", "PUT", "DELETE"
                );
    }
}
