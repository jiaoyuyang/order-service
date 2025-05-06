package com.pingan.orderservice.order.domain;

import com.pingan.orderservice.config.DataConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

@DataR2dbcTest //标记该测试类主要关注R2DBC组件
@Import(DataConfig.class) //导入所需要的R2DBC配置，以启用数据库审计功能
@Testcontainers //激活测试容器的自动化启动和清理
public class OrderRepositoryR2dbcTests {
    @Container //标记用于测试的PostgreSQL容器
    static PostgreSQLContainer<?> postgresql =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.4"));

    @Autowired
    private OrderRepository orderRepository;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {//重写 R2DBC 和 Flyway 配置以指向测试 PostgreSQL 实例
        registry.add("spring.r2dbc.url", OrderRepositoryR2dbcTests::r2dbcUrl);
        registry.add("spring.r2dbc.username", postgresql::getUsername);
        registry.add("spring.r2dbc.password", postgresql::getPassword);
        registry.add("spring.flyway.url", postgresql::getJdbcUrl);
    }

    private static String r2dbcUrl(){ //构建 R2DBC 连接字符串
        return String.format("r2dbc:postgresql://%s:%s/%s",
                postgresql.getContainerIpAddress(),
                postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                postgresql.getDatabaseName());
    }

    @Test
    void createRejectedOrder() {
        var rejectedOrder = OrderService.buildRejectedOrder("1234567890", 3);
        StepVerifier.create(orderRepository.save(rejectedOrder))//使用 OrderRepository 返回的对象来初始化一个 StepVerifier 对象
                .expectNextMatches(
                        order -> order.status().equals(OrderStatus.REJECTED))
                .verifyComplete();
    }
}
