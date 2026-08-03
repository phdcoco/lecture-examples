package com.growmighty.lectures.firstday.product.infrastructure.search;

import com.growmighty.lectures.firstday.product.domain.Product;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;

@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/product-settings.json")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long sellerId;

    @MultiField(
        mainField = @Field(type = FieldType.Text,
        analyzer = "korean_index",
        searchAnalyzer = "korean_search"),
        otherFields = {
            @InnerField(suffix = "auto", type = FieldType.Text,
            analyzer = "autocomplete_index",
            searchAnalyzer = "korean_index")
        }
    )
    private String name;

    @Field(type = FieldType .Text, analyzer = "korean_index", searchAnalyzer = "korean_search")
    private String description;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Long)
    private long salesCount;

    public static ProductDocument from(Product p) {
        return new ProductDocument(p.getId(), p.getSellerId(), p.getName(), p.getDescription(), p.getPrice(), p.getStatus().name(), p.getSalesCount());
    }
}
