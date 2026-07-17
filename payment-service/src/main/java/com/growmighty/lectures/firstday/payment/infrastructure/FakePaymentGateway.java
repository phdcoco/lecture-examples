package com.growmighty.lectures.firstday.payment.infrastructure;

import com.growmighty.lectures.firstday.payment.application.PaymentGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

// Bean 생성
@RefreshScope
// config-repo 값을 바꾸고 POST /actuator/refresh 하면
// 재시작 없이 지연을 켜고 끌 수 있다.
@Component
public class FakePaymentGateway implements PaymentGateway {
    private final AtomicLong sequence = new AtomicLong(1);

    // 실습용 강제 지연. 만약 PG사의 사정으로 인해 결제가 느려진다면 어떻게 대응할 것인가?
    @Value("${payment.demo.delay-ms:0}")
    private long delayMs;

    // approve, cancel 모두에게 delayMs만큼의 지연이 발생한다.
    @Override
    public PgApproval approve(BigDecimal amount) {
        simulateSlowPg();
        String transactionId = "PG-" + sequence.getAndIncrement();
        return new PgApproval(transactionId);
    }

    @Override
    public void cancel(String pgTransactional) {
        simulateSlowPg();
    }

    private void simulateSlowPg() {
        if (delayMs <= 0) return;
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
