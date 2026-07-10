package com.growmighty.lectures.firstday.settlement.batch;

// 읽어온 주문 1건을 정산 1건으로 가공한다.

import com.growmighty.lectures.firstday.settlement.domain.Settlement;
import com.growmighty.lectures.firstday.settlement.domain.SettlementRepository;
import com.growmighty.lectures.firstday.settlement.read.Order;
import com.growmighty.lectures.firstday.settlement.read.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

@Component
@StepScope // 원래 Component는 싱글턴이지만 지금은 배치이므로 배치 단위로 빈을 생성하고 삭제한다.
@RequiredArgsConstructor
public class OrderToSettlementProcessor implements ItemProcessor<Order, Settlement> {
    private static final BigDecimal FEE_RATE = new BigDecimal("0.03");

    private final SettlementRepository settlementRepository;
    private final SettlementFaultBox faultBox;

    // 실제 정산 건수, 스킵 제외. 이걸로 멱등성을 실현했는지 확인 가능.
    private final AtomicLong produced = new AtomicLong();

    @Override
    public Settlement process(Order order) {
        // 1차 방어 : 비즈니스 레벨의 방어
        // 멱등성 실현 : 이미 정산된 주문이면 건너뛰어라. -> 재실행/재시작 시 중복 정산을 방지한다.
        if (settlementRepository.existsByOrderId(order.getId())) {
            return null;
        }

        // 2차 방어
        // 멱등성 실현 : 상태를 확인하고 정산 대상이 아니면 넘어간다.
        if (order.getStatus() != OrderStatus.PAID || order.getPaymentId() == null) {
            return null;
        }

        // 정해진 건수를 넘는 순간 강제로 오류를 발생시켜 멱등성을 깨뜨리려 시도한다.
        long n = produced.incrementAndGet();
        if (faultBox.armed() && n > faultBox.failAfter()) {
            throw new SettlementFaultException(
                "의도적인 장애 발동 : %d건 정산 직후 강제 실패. 발현된 해당 청크는 롤백된다.".formatted(faultBox.failAfter()));
        }

        BigDecimal amount = order.getTotalAmount().getValue();
        return Settlement.of(order.getId(), order.getPaymentId(), amount, FEE_RATE);
    }
}
