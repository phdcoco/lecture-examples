package com.growmighty.lectures.firstday.order.infrastructure.client;

import com.growmighty.lectures.firstday.order.application.port.ProductPort;
import com.growmighty.lectures.firstday.order.application.port.dto.ProductSnapshot;
import com.growmighty.lectures.firstday.order.infrastructure.client.dto.ApiResponseBody;
import com.growmighty.lectures.firstday.order.infrastructure.client.dto.ProductApiData;
import com.growmighty.lectures.firstday.order.infrastructure.client.dto.StockChangeBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// 계약서 작성
// 여기서 name은 Eureka 전화번호부에 올라간 이름이다. URL이 아니다!
// Hexagonal + Feign + ACL, ProductPort를 FeignClient가 직접 구현한다.
@FeignClient(name = "product-service")
public interface ProductFeignClient extends ProductPort {

    // 실제 HTTP 계약 - Feign이 프록시로 구현
    // ApiResponseBody와 API DTO(ProductApiData)는 HTTP 세계의 언어라서 여기에 남긴다.
    @GetMapping("/products/{productId}")
    ApiResponseBody<ProductApiData> fetchProduct(@PathVariable("productId") Long productId);

    @PostMapping("/products/{productId}/decrease-stock")
    void sendDecreaseStock(@PathVariable("productId") Long productId, @RequestBody StockChangeBody body);

    @PostMapping("/products/{productId}/restore-stock")
    void sendRestoreStock(@PathVariable("productId") Long productId, @RequestBody StockChangeBody body);

    /*
    받는 쪽 선언을 하는 일반적인 컨트롤러가 아니라, 보내는 쪽을 선언한다.
    product-service의 ProductController 시그니처를 베끼는 것임.
    구현 코드는? 없다. JPA처럼 Feign이 다 해준다. ProductHttpClient가 가벼워질 것이다.

    @GetMapping("/products/{productId}")
    ApiResponseBody<ProductApiData> getProduct(@PathVariable("productId") Long productId);
    // ParameterizedTypeReference 없이 제네릭 타입을 그냥 반환 타입에 넣어버린다.
    @PostMapping("/products/{productId}/decrease-stock")
    void decreaseStock(@PathVariable("productId") Long productId, @RequestBody StockChangeBody body);

    @PostMapping("/products/{productId}/restore-stock")
    void restoreStock(@PathVariable("productId") Long productId, @RequestBody StockChangeBody body);

     */

    // 포트 구현 (default 메서드 -> 순수 자바다. HTTP 프록시 아니다.)
    // API DTO를 도메인 언어로 번역하는 것. 이것을 ACL이라 하고 여기서 한다.
    // 이 번역 계층으로 인해 product의 응답 형태가 바뀌어도 여기서 다 번역된다.
    @Override
    default ProductSnapshot getProduct(Long productId) {
        ProductApiData data = fetchProduct(productId).data(); // 여기서 Feign이 HTTP를 호출한다.
        return new ProductSnapshot(
            data.id(),
            data.name(),
            data.price(),
            data.stockQuantity(),
            data.orderable()
        );
    }

    @Override
    default void decreaseStock(Long productId, int quantity) {
        sendDecreaseStock(productId, new StockChangeBody(quantity));
    }

    @Override
    default void restoreStock(Long productId, int quantity) {
        sendRestoreStock(productId, new StockChangeBody(quantity));
    }
}
