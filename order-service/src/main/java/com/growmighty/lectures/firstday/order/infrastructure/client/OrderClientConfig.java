
// Feign이 다 해 준다. 이제 이 파일은 필요 없다.


/*package com.growmighty.lectures.firstday.order.infrastructure.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
*/
/*
    결과적으로 등록되는 Beam은
    - RestClient.Builder
    - RestClient.Builder (LoadBalanced)
    - RestClient (product)
    - RestClient (payment)
 */
/*
@Configuration // Bean을 생성하는 클래스가 있어요!
public class OrderClientConfig {
    @Bean
    @Primary // 기본 빌더. 지금 RestClient.Builder가 두 개라서 어느 걸 주입해야 할 지 모른다.
    // Eureka는 실제 URL로 통신해야 한다. 로드벨런서 빌더를 사용하면 localhost를 서비스 이름으로 해석하려 함.
    // 따라서 이 빈은 Eureka 용이다!
    RestClient.Builder plainRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @LoadBalanced // 얘는 product-service 이런 이름을 서비스 이름으로 인식한다.
    // 커스텀 이름을 실제 ip로 변환해주는 아이라고 보면 된다.
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient paymentRestClient(
        // 빌더가 두 개 있었는데, LoadBalanced 붙였으니 밑에 꺼가 쓰이겠죠?
        // 안 붙였다면 Primary였던 빈이 쓰였을 것이다!
        @LoadBalanced RestClient.Builder builder,
        @Value("${order.client.payment-base-url:http://payment-service}") String baseUrl) {
        // 원본 빌더는 건드리지 않고 복사해서 사용한다.
        return builder.clone().baseUrl(baseUrl).build();
    }

    @Bean
    RestClient productRestClient(
            @LoadBalanced RestClient.Builder builder,
            @Value("${order.client.product-base-url:http://product-service}") String baseUrl) {
        return builder.clone().baseUrl(baseUrl).build();
    }


}
*/
