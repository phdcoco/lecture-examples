package com.growmighty.lectures.firstday.product.infrastructure.search;

import com.growmighty.lectures.firstday.product.application.port.ProductIndexPort;
import com.growmighty.lectures.firstday.product.domain.Product;
import com.growmighty.lectures.firstday.product.infrastructure.ai.ProductEnrichService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductIndexAdapter implements ProductIndexPort {

    private final ProductSearchRepository searchRepository;
    private final ProductEnrichService enrichService;

    @Override
    public void indexAll(List<Product> products) {
        long start = System.currentTimeMillis();

        List<ProductDocument> docs = new ArrayList<>();
        int done = 0;
        for (Product p : products) {
            var enriched = enrichService.enrich(p);
            docs.add(ProductDocument.from(p, enriched.attributes(), enriched.embedding()));
            if (++done % 10 == 0) {
                log.info("백필 진행 {} / {} ({}초 경과)", done, products.size(), System.currentTimeMillis() - start / 1000);
            }

            searchRepository.saveAll(docs);
            log.info("백필 색인 완료: {}건, 총 {}초", docs.size(), (System.currentTimeMillis() - start) / 1000);
        }

    }
}
