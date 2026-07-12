package com.growmighty.lectures.firstday.order.infrastructure.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/*
    모놀리식은 자기 자신을 호출했지만, 이제 상품과 결제가 다른 프로세스에 있다.
    이제 자기 자신이 아닌, 목적지별 RestClient 2개로 확장한다.
 */
@Configuration // Bean을 생성하는 클래스가 있어요!
public class OrderClientConfig {

    @Bean
    RestClient productRestClient(
        @Value("${order.client.product-base-url:http://localhost:8081}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    RestClient paymentRestClient(
        @Value("${order.client.payment-base-url:http://localhost:8082}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
