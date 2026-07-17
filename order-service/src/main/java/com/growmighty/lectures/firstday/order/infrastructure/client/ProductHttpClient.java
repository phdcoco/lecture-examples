
/*

이제 이 파일은 필요 없다.

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

    // RestClient 대신 Feign 프록시를 주입받는다.
    private final ProductFeignClient productFeignClient;


    @Override
    public ProductSnapshot getProduct(Long productId) {

        // Feign이 HTTP 관련된 것 알아서 다 해줌. 대신 그걸 번역하는 건 우리가 해야 함.
        ProductApiData data = productFeignClient.getProduct(productId).data();
        return new ProductSnapshot(
                data.id(),
                data.name(),
                data.price(),
                data.stockQuantity(),
                data.orderable()
        );
    }

    // 나머지 두 개도 다 Feign에게 위임하자.
    @Override
    public void decreaseStock(Long productId, int quantity) {
        productFeignClient.decreaseStock(productId, new StockChangeBody(quantity));
    }

    @Override
    public void restoreStock(Long productId, int quantity) {
        productFeignClient.restoreStock(productId, new StockChangeBody(quantity));
    }

}
 */
