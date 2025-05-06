package com.pingan.orderservice.order.web;

import com.pingan.orderservice.order.domain.Order;
import com.pingan.orderservice.order.domain.OrderService;
import com.pingan.orderservice.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.assertj.core.api.Assertions.assertThat;
import reactor.core.publisher.Mono;

import static org.mockito.BDDMockito.given;

@WebFluxTest(OrderController.class) //标识该测试类主要关注 Spring WebFlux 组件，具体来讲，针对的是 OrderController
public class OrderControllerWebFluxTests {
    @Autowired
    private WebTestClient webClient; //具有额外特性的 WebClient 变种，会使 RESTful 服务的测试更便捷
    @MockBean //添加 mock OrderService 到 Spring 应用上下文中
    private OrderService orderService;
    @Test
    void whenBookNotAvailableThenRejectOrder() {
        var orderRequest = new OrderRequest("1234567890", 3);
        var expectedOrder = OrderService.buildRejectedOrder(
                orderRequest.isbn(), orderRequest.quantity());

        given(orderService.submitOrder(
                orderRequest.isbn(), orderRequest.quantity())
        ).willReturn(Mono.just(expectedOrder)); // 定义 OrderService mock bean 的预期行为

        webClient
                .post()
                .uri("/orders/")
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful() //预期订单创建成功
                .expectBody(Order.class).value(actualOrder -> {
                    assertThat(actualOrder).isNotNull();
                    assertThat(actualOrder.status()).isEqualTo(OrderStatus.REJECTED);
                });
    }
}
