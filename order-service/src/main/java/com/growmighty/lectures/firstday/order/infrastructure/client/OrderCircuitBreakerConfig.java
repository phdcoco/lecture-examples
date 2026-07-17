package com.growmighty.lectures.firstday.order.infrastructure.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

// 서킷브레이커 발동 수칙
@Configuration
public class OrderCircuitBreakerConfig {

    // 모든 서킷브레이커의 기본 수칙.
    // 실무에서는 창 크기 100, 최소 호출 100, 대기 60초 등 훨씬 진득하게 지켜본다.
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCircuitBreakerCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            // 이 시간 안에 응답이 없으면 실패로 치고 스레드를 푼다.
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                    .build())
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .slidingWindowSize(10) // 최근 10번의 호출을 기억한다.
                .minimumNumberOfCalls(4) // 최소 4번은 지켜본 뒤에야 판단한다.
                .waitDurationInOpenState(Duration.ofSeconds(10)) // 10초 버틴 뒤 반쯤 열어본다.
                .permittedNumberOfCallsInHalfOpenState(2) // 시험 호출은 2번만 통과시킨다.
                .build())
            .build());
    }
}
