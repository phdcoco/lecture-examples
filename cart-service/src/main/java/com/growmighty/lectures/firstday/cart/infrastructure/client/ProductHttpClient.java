package com.growmighty.lectures.firstday.cart.infrastructure.client;

import com.growmighty.lectures.firstday.cart.application.port.ProductPort;
import com.growmighty.lectures.firstday.cart.application.port.dto.ProductSnapshot;
import com.growmighty.lectures.firstday.cart.infrastructure.client.dto.ApiResponseBody;
import com.growmighty.lectures.firstday.cart.infrastructure.client.dto.ProductApiData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// Cart 서비스가 Product 서비스에 HTTP 요청을 보내 상품 정보 가져오는 클라이언트
// Application Layer -> ProductPort(인터페이스) <- implements ProductHttpClient
@Slf4j
@Component // ProductPort를 누군가 데려오면 실제로는 이 클래스가 빈으로 주입된다.
@RequiredArgsConstructor
public class ProductHttpClient implements ProductPort {

    private final RestClient productRestClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Override
    public ProductSnapshot getProduct(Long productId) {
        // 서킷브레이커를 앞에다가 두고 서킷브레이커 넘어가면 그 때 진짜 일을 한다.
        return circuitBreakerFactory.create("product").run(
            () -> callGetProduct(productId),
            cause -> optimisticFallback(productId, cause)
        );
    }

    private ProductSnapshot callGetProduct(Long productId) {
        // HTTP GET 요청 시작.
        ApiResponseBody<ProductApiData> body = productRestClient.get()
            // baseURI에다 요 URI를 붙여서 최종 URL을 만들고 이때 로드밸런서가 baseURI를 IP로 바꿔준다.
            .uri("/products/{productId}", productId)
            .retrieve() // HTTP가 요청을 보낸다.
            .body(new ParameterizedTypeReference<>() {
                  } // 응답 JSON을 자바 객체로 변환한다.
                // 제네릭 타입 소거가 발생하기 때문에 인자를 넘겨 제네릭 타입을 유지하도록 한다.
            );
        ProductApiData data = body.data(); // 제네릭 타입 T로 되어있는 레코드를 꺼낸다.
        return new ProductSnapshot(data.id(), data.orderable());

    }

    private ProductSnapshot optimisticFallback(Long productId, Throwable cause) {
        log.warn("상품 확인 실패 -> 낙관적으로 담기 진행. productId = {}, 원인 = {}", productId, cause.toString());

        // 장바구니 담기는 구매가 아니다. -> 최종 검증은 어차피 주문 시점에 다시 한다.
        // 그래서 확인이 정확하게 안 되면 판매 중이라고 가정하고 일단 담는다.
        // 혹여나 판매 중이 아니어도 주문 시점에서 잡아 줄 것을 믿는다.
        // 사용자는 product-service가 죽었는지 사실조차 모르게 된다. 이것을 우아한 실패라고 한다.

        return new ProductSnapshot(productId, true);
    }
}
