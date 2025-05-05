package com.pingan.orderservice.order.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
//扩展提供CRUD操作的反应式资源库，声明要管理的实体类型（Order）及其主键的类型（Long）
}
