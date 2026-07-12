package com.growmighty.lectures.firstday.seller.domain;

import java.util.Optional;

// DIP 위해 JPA 삭제
public interface SellerRepository {
    Seller save(Seller seller);

    Optional<Seller> findById(Long id);

    boolean existsByUserId(Long userId);
}
