package com.growmighty.lectures.firstday.tangledmonolith.settlement.batch;

// 읽어온 주문 1건을 정산 1건으로 가공한다.기

import com.growmighty.lectures.firstday.tangledmonolith.order.domain.OrderStatus;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.domain.Settlement;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderToSettlementProcessor implements ItemProcessor<Order, Settlement> {
    private static final BigDecimal FEE_RATE = new BigDecimal("0.03");

    @Override
    public Settlement process(Order order) {
        if (order.getStatus() != OrderStatus.PAID || order.getPaymentId() == null) {
            return null; // 정산 대상이 아님.
        }
        BigDecimal amount = order.getTotalAmount().getValue();
        return Settlement.of(order.getId(), order.getPaymentId(), amount, FEE_RATE);
    }

}
