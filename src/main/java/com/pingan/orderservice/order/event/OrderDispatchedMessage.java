package com.pingan.orderservice.order.event;

public record OrderDispatchedMessage(
        Long orderId
) { }
