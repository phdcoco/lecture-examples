package com.growmighty.lectures.firstday.cart.infrastructure.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CartCircuitBreakerConfig {
    // Circuit Breaker 차단 규칙
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCircuitBreakerCustomizer() {
        return factory -> factory.configureDefault(id -> new
            Resilience4JConfigBuilder(id)
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3)) // 3초 동안 스레드 붙잡고 있다가 안 되면 놔 줌.
                .build())
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                    .slidingWindowSize(10) // 최근 10 번의 호출을 기억하는 창
                    .minimumNumberOfCalls(50) // 최소 4번은 지켜본 뒤 판단한다.
                    .waitDurationInOpenState(Duration.ofSeconds(10)) // 차단기가 내려가고 10초 버틴 뒤 HALF_OPEN 해 본다.
                    .permittedNumberOfCallsInHalfOpenState(2) // 시험 호출은 2번만 통과시킨다.
                    .build())
                .build());
    }
}
