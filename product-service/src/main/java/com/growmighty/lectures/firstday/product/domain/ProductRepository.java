package com.growmighty.lectures.firstday.product.domain;

import java.util.List;
import java.util.Optional;

// DIP 구현
public interface ProductRepository {
    Product save(Product product);

    Optional<Product> findById(Long id);

    List<Product> findAll(); // 재색인용
}
