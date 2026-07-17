package com.growmighty.lectures.firstday.cart.application.port.dto;

// Cart가 필요로 하는 만큼만 담은 상품 스냅샷.
// 장바구니 담기 판단에 필요한 값만 노출한다.
public record ProductSnapshot(
        Long productId,
        boolean orderable
) {
}
