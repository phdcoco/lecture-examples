package com.growmighty.lectures.firstday.cart.infrastructure.client.dto;

import java.math.BigDecimal;

// product-service의 GET /products/{id}의 응답 데이터 형식 정의
public record ProductApiData (
    Long id,
    Long sellerId,
    String name,
    BigDecimal price,
    int stockQuantity,
    String status,
    boolean orderable
){
}
