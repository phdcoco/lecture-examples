package com.growmighty.lectures.firstday.product.infrastructure.search;

import com.growmighty.lectures.firstday.product.domain.Product;
import com.growmighty.lectures.firstday.product.domain.ProductRepository;
import com.growmighty.lectures.firstday.product.domain.event.ProductChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductIndexer {

    private final ProductRepository productRepository;
    private final ProductSearchRepository searchRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void handle(ProductChangedEvent event) {
        try {
            productRepository.findById(event.productId())
                .ifPresent(product -> searchRepository.save(ProductDocument.from(product)));
            log.info("색인 완료 productId= {}", event.productId());
        } catch (Exception e) {
            log.error("검색 색인 실패 productId= {} - 재색인 배치로 보정 필요", event.productId(), e);
        }
    }
}
