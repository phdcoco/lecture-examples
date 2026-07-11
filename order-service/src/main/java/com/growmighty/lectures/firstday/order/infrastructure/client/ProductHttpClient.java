package com.growmighty.lectures.firstday.order.infrastructure.client;

import com.growmighty.lectures.firstday.order.application.port.ProductPort;
import com.growmighty.lectures.firstday.order.application.port.dto.ProductSnapshot;
import com.growmighty.lectures.firstday.order.infrastructure.client.dto.ApiResponseBody;
import com.growmighty.lectures.firstday.order.infrastructure.client.dto.ProductApiData;
import com.growmighty.lectures.firstday.order.infrastructure.client.dto.StockChangeBody;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ProductHttpClient implements ProductPort {
    // productRestClient를 주입받도록 필드명 변경.
    // 필드명이 빈 이름과 일치하면 스프링이 이름에 맞는 빈을 알아서 주입해 준다.
    private final RestClient productRestClient;


    @Override
    public ProductSnapshot getProduct(Long productId) {
        ApiResponseBody<ProductApiData> body = productRestClient.get()
                .uri("/products/{productId}", productId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        ProductApiData data = body.data();
        return new ProductSnapshot(
                data.id(),
                data.name(),
                data.price(),
                data.stockQuantity(),
                data.orderable()
        );
    }

    @Override
    public void decreaseStock(Long productId, int quantity) {
        productRestClient.post()
                .uri("/products/{productId}/decrease-stock", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new StockChangeBody(quantity))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void restoreStock(Long productId, int quantity) {
        productRestClient.post()
                .uri("/products/{productId}/restore-stock", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new StockChangeBody(quantity))
                .retrieve()
                .toBodilessEntity();
    }

}
