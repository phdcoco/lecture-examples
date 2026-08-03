package com.growmighty.lectures.firstday.product.infrastructure.ai;

import java.util.List;

public record ProductAttributes(
    List<String> occasions,
    List<String> styles,
    List<String> seasons,
    String material,
    boolean rainFriendly
) {
    public static ProductAttributes empty() {
        return new ProductAttributes(List.of(), List.of(), List.of(), null, false);
    }
}
