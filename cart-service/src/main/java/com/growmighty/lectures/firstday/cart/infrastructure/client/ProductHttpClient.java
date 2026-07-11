package com.growmighty.lectures.firstday.cart.infrastructure.client;

import com.growmighty.lectures.firstday.cart.application.port.ProductPort;
import com.growmighty.lectures.firstday.cart.application.port.dto.ProductSnapshot;
import com.growmighty.lectures.firstday.cart.infrastructure.client.dto.ApiResponseBody;
import com.growmighty.lectures.firstday.cart.infrastructure.client.dto.ProductApiData;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// Cart 서비스가 Product 서비스에 HTTP 요청을 보내 상품 정보 가져오는 클라이언트
// Application Layer -> ProductPort(인터페이스) <- implements ProductHttpClient
@Component // ProductPort를 누군가 데려오면 실제로는 이 클래스가 빈으로 주입된다.
@RequiredArgsConstructor
public class ProductHttpClient implements ProductPort {

    private final RestClient productRestClient;

    @Override
    public ProductSnapshot getProduct(Long productId) {
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
        return new ProductSnapshot(data.id(), data.orderable()); // 외부 서비스인 Product의 DTO를 Cart의 DTO로 변환한다.
        // 외부 서비스의 DTO를 애플리켘이션 내부까지 가져오지 않는 것이 원칙이다.
        // 따라서 외부 서비스의 응답 형식 DTO까 바뀌더라도 Cart 서비스 코드에는 영향이 가지 않는다. 결합도 하락.
    }
}
