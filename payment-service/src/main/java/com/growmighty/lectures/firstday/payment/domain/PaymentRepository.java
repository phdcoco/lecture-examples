package com.growmighty.lectures.firstday.payment.domain;

import java.util.Optional;

// DIP 구현!
public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);
}
