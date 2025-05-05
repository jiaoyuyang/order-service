package com.pingan.orderservice.order.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("orders") //配置order对象和orders表之间的映射
public record Order(
        @Id
        Long id, //实体的主键
        String bookIsbn,
        String bookName,
        Double bookPrice,
        Integer quantity,
        OrderStatus status,
        @CreatedDate
        Instant createdDate, //实体的创建时间
        @LastModifiedDate
        Instant lastModifiedDate,
        @Version
        int version //实体的版本号
) {
    public static Order of(
            String bookIsbn, String bookName, Double bookPrice, Integer quantity, OrderStatus status) {
        return new Order(null, bookIsbn, bookName, bookPrice, quantity, status, null, null, 0);
    }
}
