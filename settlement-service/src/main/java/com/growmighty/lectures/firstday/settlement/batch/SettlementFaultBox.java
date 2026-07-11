package com.growmighty.lectures.firstday.settlement.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementFaultBox {
    // volatile로 메모리 가시성 확보
    private volatile long failAfter = 0; // 이 건수를 초과해 정산을 만들려는 순간 장애 발생. 정상은 당연히 이 값이 0이어야...

    // 지정한 건수만큼 정산한 뒤 터지도록 장전
    public void arm(long failAfter) {
        this.failAfter = failAfter;
        log.warn("[FAULT] 장애 장전: {}건 정산 후 강제로 실패하게 하여 중복 결제 유도", failAfter);
    }

    public void disarm() {
        if (failAfter > 0) {
            log.warn("[fault] 장애 해제, 정상화 되었음.");
        }
        this.failAfter = 0;
    }

    public boolean armed() {
        return failAfter > 0;
    }

    public long failAfter() {
        return failAfter;
    }
}
