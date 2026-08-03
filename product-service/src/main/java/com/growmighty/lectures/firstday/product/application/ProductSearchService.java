package com.growmighty.lectures.firstday.product.application;

import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import com.growmighty.lectures.firstday.product.application.dto.ProductSearchResult;
import com.growmighty.lectures.firstday.product.application.port.ProductSearchPort;
import com.growmighty.lectures.firstday.product.infrastructure.search.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchPort searchPort;

    public List<ProductSearchResult> search(String keyword, Double minPrice, Double maxPrice, int page, int size) {
        return searchPort.search(keyword, minPrice, maxPrice, page, size);
    }

    public List<String> autocomplete(String prefix) {
        return searchPort.autocomplete(prefix);
    }

    public List<ProductSearchResult> semanticSearch(String keyword, int size) {
        return searchPort.semanticSearch(keyword, size);
    }

    public List<ProductSearchResult> hybridSearch(String keyword, int size) {
        return searchPort.hybridSearch(keyword, size);
    }

}
