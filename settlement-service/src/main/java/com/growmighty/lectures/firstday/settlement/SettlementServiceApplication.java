package com.growmighty.lectures.firstday.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 각 서비스마다 자기만의 @SpringBootApplication이 필요하다.
// scanBasePackages로 자신의 패키지 아래뿐만 아닌 다른 곳도 스캔한다.
@SpringBootApplication(scanBasePackages = {
    "com.growmighty.lectures.firstday.settlement",
    "com.growmighty.lectures.firstday.common" // 요거 등록 안하면 커스텀예외가 빈으로 등록되지 않는다.
})
public class SettlementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettlementServiceApplication.class, args);
    }
}
