package com.growmighty.lectures.firstday.product.application.port;

import com.growmighty.lectures.firstday.product.domain.Product;

import java.util.List;

public interface ProductIndexPort {
    void indexAll(List<Product> products);
}
