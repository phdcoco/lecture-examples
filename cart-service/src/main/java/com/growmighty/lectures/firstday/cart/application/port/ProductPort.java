package com.growmighty.lectures.firstday.cart.application.port;

import com.growmighty.lectures.firstday.cart.application.port.dto.ProductSnapshot;

// Cart가 쓰니까 Cart의 품으로.
// Cart는 Product 클래스 모른다. 인터페이스로만 상품을 바라보고
// 실제 통신은 infrastructure의 HTTP 클라이언트가 담당한다.
public interface ProductPort {
    ProductSnapshot getProduct(Long productId);
}
