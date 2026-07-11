package com.growmighty.lectures.firstday.product.presentation;

// 배너 확인용 컨트롤러

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RefreshScope // /actuator/refresh 신호가 오면 이 빈을 버리고 새로 만든다.
// 그때 @Value가 새 설정값으로 다시 주입된다. 재시작 없는 설정 변경 가능.
@RestController
public class BannerController {

    // config-repo/product-service.yml의 값이 주입된다.
    // : 뒤에 있는 값은 만약 못 받았을 때의 기본 값.
    @Value("${product.banner.message: 이벤트 준비 중}")
    private String bannerMessage;

    @GetMapping("/products/banner")
    public Map<String, String> banner() {
        return Map.of("message", bannerMessage);
    }
}
