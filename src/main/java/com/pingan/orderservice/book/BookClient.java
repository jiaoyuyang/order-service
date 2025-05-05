package com.pingan.orderservice.book;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class BookClient {
    private static final String BOOKS_ROOT_API = "/books/";
    private final WebClient webClient;

    public BookClient(WebClient webClient) {
        this.webClient = webClient; //前文配置的WebClient Bean
    }

    public Mono<Book> getBookByIsbn(String isbn) {
        return webClient
                .get() //请求应该使用GET方法
                .uri(BOOKS_ROOT_API + isbn) //请求的目标URL是/books/{isbn}
                .retrieve() //发送请求并获取相应
                .bodyToMono(Book.class) //以Mono<Book>的形式返回要检索的对象
                .timeout(Duration.ofSeconds(3), Mono.empty())  //为GET请求设置3秒的超时
                .onErrorResume(WebClientResponseException.NotFound.class, exception -> Mono.empty()) //当接收到404响应时，返回有一个空的对象
                .retryWhen( //retryWhen()操作符放到timeout()之后意味着超时适用于每次重试操作
                        //使用指数退避作为重试策略。允许重试3次并且初始延迟为100毫秒
                        Retry.backoff(3, Duration.ofMillis(100)))
                .onErrorResume(Exception.class, exception -> Mono.empty()); //如果在三次重试后，依然出现错误的话，捕获异常并返回空对象
    }
}
