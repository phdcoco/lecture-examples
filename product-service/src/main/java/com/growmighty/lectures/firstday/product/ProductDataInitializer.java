package com.growmighty.lectures.firstday.product;

import com.growmighty.lectures.firstday.product.application.ProductService;
import com.growmighty.lectures.firstday.product.application.dto.RegisterProductCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("!test")
@RequiredArgsConstructor
// 이제 각 서비스마다 자신만의 시드 데이터를 가진다.
public class ProductDataInitializer implements CommandLineRunner {

    private final ProductService productService;

    @Override
    public void run(String... args) {
        // sellerId는 이제 다른 서비스의 식별자다. 임의 값을 사용해도 된다.
        productService.register(new RegisterProductCommand(1L, "청축 키보드", BigDecimal.valueOf(120_000), 100, "설명: 청축 키보드"));
        productService.register(new RegisterProductCommand(1L, "무선 마우스", BigDecimal.valueOf(45_000), 200, "설명: 무선 마우스"));
    }
}
