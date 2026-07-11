package com.growmighty.lectures.firstday.cart.domain;

import java.util.Optional;

// Domain으로 이동, 이때! DIP를 구현하기 위해 JpaRepository를 extends하지 않음.
// 그 과정은 infrastructure에서 진행한다.
public interface CartRepository {
    Cart save(Cart cart);

    Optional<Cart> findByUserId(Long userId);
}
