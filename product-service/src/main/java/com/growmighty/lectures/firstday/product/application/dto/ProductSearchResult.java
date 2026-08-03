package com.growmighty.lectures.firstday.product.application.dto;

import com.growmighty.lectures.firstday.product.domain.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public record ProductSearchResult(
    Long id,
    Long sellerId,
    String name,
    String description,
    BigDecimal price,
    ProductStatus status,
    long salesCount,
    List<String> occasions,
    List<String> styles,
    List<String> seasons,
    String material,
    boolean rainFriendly
) {
}
