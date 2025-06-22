package com.pingan.orderservice.order.domain;

import com.pingan.orderservice.book.Book;
import com.pingan.orderservice.book.BookClient;
import com.pingan.orderservice.order.event.OrderDispatchedMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service //构造型注解，将此类标记为Spring 管理的服务
public class OrderService {
    private final BookClient bookClient;
    private final OrderRepository orderRepository;

    public OrderService(BookClient bookClient, OrderRepository orderRepository) {
        this.bookClient = bookClient;
        this.orderRepository = orderRepository;
    }

    public Flux<Order> getAllOrders() { //使用Flux来发布多个订单
        return orderRepository.findAll();
    }

    public Mono<Order> submitOrder(String isbn, int quantity) {
        return bookClient.getBookByIsbn(isbn) //调用Catalog Service以检查图书的可用性
                .map(book -> buildRejectedOrder(book, quantity)) //如果图书可用的话，接受该订单
                .defaultIfEmpty( // 如果图书不可用的话，拒绝该订单
                        buildRejectedOrder(isbn, quantity)
                )
                .flatMap(orderRepository::save); //保存订单（可能是accented状态或rejected状态）
    }

    public static Order buildRejectedOrder(Book book, int quantity) {
        //当订单被接受的话，我们指定ISBN,图书名称（书名+作者），数量和状态。Spring Data会负责添加标识符，版本和审计元数据
        return Order.of(book.isbn(), book.title() + "-" + book.author(), book.price(), quantity, OrderStatus.ACCEPTED);

    }

    public static Order buildRejectedOrder(String bookIsbn, int quantity) {
        return Order.of(bookIsbn, null, null, quantity, OrderStatus.REJECTED);

    }

    public Flux<Order> consumeOrderDispatchedEvent(
            Flux<OrderDispatchedMessage> flux
    ){
        return flux.flatMap(message ->
                orderRepository.findById(message.orderId()))
                .map(this::buildDispathedOrder)
                .flatMap(orderRepository::save);
    }

    private Order buildDispathedOrder(Order existingOrder){
        return new Order(
                existingOrder.id(),
                existingOrder.bookIsbn(),
                existingOrder.bookName(),
                existingOrder.bookPrice(),
                existingOrder.quantity(),
                OrderStatus.DISPATCHED,
                existingOrder.createdDate(),
                existingOrder.lastModifiedDate(),
                existingOrder.version()

        );
    }
}
