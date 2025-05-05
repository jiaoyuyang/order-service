package com.pingan.orderservice.book;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

public class BookClientTests {
    private MockWebServer mockWebServer;
    private BookClient bookClient;

    @BeforeEach
    void setup() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start(); //在运行测试用例前启动mock服务器
        var webClient = WebClient.builder() //使用mock服务器的URL作为WebClient的基础URL
                .baseUrl(mockWebServer.url("/").uri().toString())
                .build();
        this.bookClient = new BookClient(webClient);
    }

    @Test
    void whenBookExistsThenReturnBook() {
        var bookIsbn = "1234567895";
        var mockResponse = new MockResponse() //定义mock服务器返回的响应
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                        "isbn": %s,
                        "title": "Title",
                        "author": "Author",
                        "price": 9.90,
                        "publisher": "Polarsophia" 
                        }                    
                        """.formatted(bookIsbn));

        mockWebServer.enqueue(mockResponse); //添加mock响应到mock服务器处理的队列中

        Mono<Book> book = bookClient.getBookByIsbn(bookIsbn);

        StepVerifier.create(book) //使用BookClient返回的对象来初始化一个StepVerifier对象
                .expectNextMatches(
                        b -> b.isbn().equals(bookIsbn)) //断言返回的Book具有所请求的ISBN
                .verifyComplete(); //检验反应式流成功完成
    }

    @AfterEach
    void clean() throws IOException {
        this.mockWebServer.shutdown(); //在完成测试后关闭mock服务器
    }

}
