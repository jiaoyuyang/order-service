package com.pingan.orderservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@Configuration //表明该类为Spring的配置源
@EnableR2dbcAuditing //为持久化实体启用D2DBC审计
public class DataConfig {
}
