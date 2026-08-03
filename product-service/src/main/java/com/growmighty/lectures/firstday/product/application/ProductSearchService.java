package com.growmighty.lectures.firstday.product.application;

import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
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

    private final ElasticsearchOperations operations;

    public List<ProductDocument> search(String keyword, Double minPrice, Double maxPrice, int page, int size) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(q -> q.functionScore(fs -> fs
                .query(inner -> inner.bool(b -> {
                    b.must(m -> m.multiMatch(mm -> mm
                        .query(keyword)
                        .fields("name^3", "description")
                        .fuzziness("AUTO")));
                    b.filter(f -> f.term(t -> t.field("status").value("ON_SALE")));
                    if (minPrice != null || maxPrice != null) {
                        b.filter(f -> f.range(r -> r.number(n -> {
                            n.field("price");
                            if (minPrice != null) n.gte(minPrice);
                            if (maxPrice != null) n.lte(maxPrice);
                            return n;
                        })));
                    }
                    return b;
                }))
                .functions(fn -> fn.fieldValueFactor(fv -> fv
                    .field("salesCount")
                    .modifier(FieldValueFactorModifier.Log1p)
                    .factor(2.0)
                    .missing(0.0)))
                .boostMode(FunctionBoostMode.Sum)
            ))
            .withPageable(PageRequest.of(page, size))
            .build();

        SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);

        return hits.getSearchHits().stream().map(h -> h.getContent()).toList();
    }

    public List<String> autocomplete(String prefix) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(q -> q.match(m -> m.field("name.auto").query(prefix)))
            .withPageable(PageRequest.of(0, 10))
            .build();
        return operations.search(query, ProductDocument.class)
            .getSearchHits().stream()
            .map(h -> h.getContent().getName())
            .distinct()
            .toList();
    }
}
