package com.pingan.orderservice.order.web;

import com.pingan.orderservice.order.domain.Order;
import com.pingan.orderservice.order.domain.OrderService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("orders")
public class OrderController {
    private final OrderService orderSrevice;

    public OrderController(OrderService orderService) {
        this.orderSrevice = orderService;
    }

    @GetMapping
    //使用Flux来发布多个订单（0..N）
    public Flux<Order> getAllOrders(){
        return orderSrevice.getAllOrders();
    }

    @PostMapping
    public Mono<Order> submitOrder(@RequestBody @Valid OrderRequest orderRequest){
        return orderSrevice.submitOrder(
                orderRequest.isbn(), orderRequest.quantity()
        );
    }

}
