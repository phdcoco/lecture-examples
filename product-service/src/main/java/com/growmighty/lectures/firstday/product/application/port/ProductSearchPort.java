package com.growmighty.lectures.firstday.product.application.port;

import com.growmighty.lectures.firstday.product.application.dto.ProductSearchResult;

import java.util.List;

public interface ProductSearchPort {
    List<ProductSearchResult> search(String keyword, Double minPrice, Double maxPrice, int page, int size);

    List<String> autocomplete(String prefix);

    List<ProductSearchResult> semanticSearch(String keyword, int size);

    List<ProductSearchResult> hybridSearch(String keyword, int size);
}
