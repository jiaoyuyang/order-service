package com.pingan.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {
    @Bean
    WebClient webClient(
            ClientProperties clientProperties,
            WebClient.Builder webClientBuilder //Spring Boot自动配置的对象，以构建WebClient bean
    ) {
        return webClientBuilder //将WebClient基础URL配置为自定义属性所声明的Catalog Service URL
                .baseUrl(clientProperties.catalogServiceUri().toString())
                .build();
    }
}
