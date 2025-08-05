package com.pingan.orderservice.order.domain;

import com.pingan.orderservice.book.Book;
import com.pingan.orderservice.book.BookClient;
import com.pingan.orderservice.order.event.OrderAcceptedMessage;
import com.pingan.orderservice.order.event.OrderDispatchedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service //构造型注解，将此类标记为Spring 管理的服务
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final BookClient bookClient;
    private final OrderRepository orderRepository;
    private final StreamBridge streamBridge;

    public OrderService(BookClient bookClient, StreamBridge streamBridge, OrderRepository orderRepository) {
        this.bookClient = bookClient;
        this.orderRepository = orderRepository;
        this.streamBridge = streamBridge;
    }

    public Flux<Order> getAllOrders() { //使用Flux来发布多个订单
        return orderRepository.findAll();
    }

    @Transactional
    public Mono<Order> submitOrder(String isbn, int quantity) {
        return bookClient.getBookByIsbn(isbn) //调用Catalog Service以检查图书的可用性
                .map(book -> buildRejectedOrder(book, quantity)) //如果图书可用的话，接受该订单
                .defaultIfEmpty( // 如果图书不可用的话，拒绝该订单
                        buildRejectedOrder(isbn, quantity)
                )
                .flatMap(orderRepository::save) //保存订单到数据库中
                .doOnNext(this::publishOrderAcceptedEvent);  //如果订单被接受的话，发布一个事件
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
        return flux //接受 OrderDispatchedMessage 对象组成的反应式流作为输入
                .flatMap(message ->
                orderRepository.findById(message.orderId())) //对于发布到流中的每个对象，从数据中读取相关订单
                .map(this::buildDispathedOrder) //将订单更新为“dispatched”状态
                .flatMap(orderRepository::save); //将更新后的订单保存到数据库中
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

    private void publishOrderAcceptedEvent(Order order){
        if(!order.status().equals(OrderStatus.ACCEPTED)){
            return; //如果订单没有被接受，不执行任何操作
        }

        var orderAcceptedMessage = new OrderAcceptedMessage(order.id());//构建一条消息以通知该订单已被接受
        log.info("Sending order accepted event with id:{}", order.id());
        var result = streamBridge.send("acceptOrder-out-0",
                orderAcceptedMessage);//将消息显示发送至 acceptOrder-out-0 绑定
        log.info("Result of sending data for order with id {}:{}", order.id(),result);
    }
}
