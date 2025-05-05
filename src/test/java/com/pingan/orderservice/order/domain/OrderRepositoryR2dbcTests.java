package com.pingan.orderservice.order.domain;

import com.pingan.orderservice.config.DataConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@DataR2dbcTest //标记该测试类主要关注R2DBC组件
@Import((DataConfig.class)) //导入所需要的R2DBC配置，以启用数据库审计功能
@Testcontainers //激活测试容器的自动化启动和清理
public class OrderRepositoryR2dbcTests {
    @Container //标记用于测试的PostgreSQL容器
    static PostgreSQLContainer<?> postgresql =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.4"));

    @Autowired
    private OrderRepository orderRepository;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", OrderRepositoryR2dbcTests::r2dbcUrl);
    }
}
