package com.growmighty.lectures.firstday.cart.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class CartClientConfig {

    @Bean
    @Primary
    RestClient.Builder plainRestClientBuilder() {
        return RestClient.builder();
    }

    // 로드밸런서 빌더: 이 빌더로 만든 RestClient만 URL의 호스트 자리를 서비스 이름으로 해석한다.
    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient productRestClient(
        @LoadBalanced RestClient.Builder builder, // 로드밸런서 빌더를 데려온다.
        @Value("${cart.client.product-base-url:http://product-service}") String baseUrl
    ) {
        return builder.clone().baseUrl(baseUrl).build();
    }
}
