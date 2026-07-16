package com.growmighty.lectures.firstday.order.infrastructure.client;

import com.growmighty.lectures.firstday.common.exception.ServiceUnavailableException;
import com.growmighty.lectures.firstday.order.application.port.PaymentPort;
import com.growmighty.lectures.firstday.order.application.port.dto.PaymentResult;
import com.growmighty.lectures.firstday.order.infrastructure.client.dto.ApiResponseBody;
import com.growmighty.lectures.firstday.order.infrastructure.client.dto.PayBody;
import com.growmighty.lectures.firstday.order.infrastructure.client.dto.PaymentApiData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

// 결제 호출을 서킷 브레이커라는 문지기 뒤로 옮긴다.
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentHttpClient implements PaymentPort {
    private final RestClient paymentRestClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Override
    public PaymentResult pay(BigDecimal amount) {
        return circuitBreakerFactory.create("payment").run(
            () -> callPay(amount), // 평소에는 이걸 실행한다.
            this::payFallback); // 실패했거나 차단기가 올려져 있어 거부
    }

    // 기존 pay()를 여기로 옮겨온다.
    private PaymentResult callPay(BigDecimal amount) {
        ApiResponseBody<PaymentApiData> body = paymentRestClient.post() // POST를 보낸다.
            .uri("/payments") // POST /payment
            .contentType(MediaType.APPLICATION_JSON) // Content-Type: application/json
            .body(new PayBody(amount)) // JSON의 Body 구성. "amount":10000 이렇게 뜰 것이다.
            .retrieve() // 라는 요청을 실제로 보낸다.
            .body(new ParameterizedTypeReference<>() { // 응답을 JSON에서 Java 객체로 변환한다.
            });

        PaymentApiData data = body.data();
        return new PaymentResult(data.paymentId(), data.amount(), data.status());
    }

    private PaymentResult payFallback(Throwable cause) {
        // 결제 호출이 실제로 실패 혹은 타임아웃
        // 차단기가 OPEN 상태라 무조건 실패. 정상이었을 수도 있는거임.
        log.warn("결제 호출 실패 -> fallback 실행. 원인: {}", cause.toString());

        // 스레드 잡고 늘어지는 게 아니라, 놔 주고 실패했다. 이따 다시 해봐라 라고 깔끔하게 전달함.
        throw new ServiceUnavailableException("결제 서비스가 일시적으로 응답하지 않습니다. 잠시 후 다시 시도해 주세요.");
    }

    @Override
    public void cancel(Long paymentId) {
        paymentRestClient.post()
                .uri("/payments/{paymentId}/cancel", paymentId)
                .retrieve()
                .toBodilessEntity(); // 응답은 필요 없다.
    }
}
