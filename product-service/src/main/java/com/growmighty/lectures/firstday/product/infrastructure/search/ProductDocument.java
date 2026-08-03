package com.growmighty.lectures.firstday.product.infrastructure.search;

import com.growmighty.lectures.firstday.product.domain.Product;
import com.growmighty.lectures.firstday.product.infrastructure.ai.ProductAttributes;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.util.List;

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

    @Field(type = FieldType.Keyword)      // ★ 필터·집계용 → keyword (오전 3-5 "걸러낼 값이면 keyword")
    private List<String> occasions;

    @Field(type = FieldType.Keyword)
    private List<String> styles;

    @Field(type = FieldType.Keyword)
    private List<String> seasons;

    @Field(type = FieldType.Keyword)
    private String material;

    @Field(type = FieldType.Boolean)
    private boolean rainFriendly;

    @Field(type = FieldType.Dense_Vector, dims = 1024)   // ★ bge-m3 = 1,024차원. 모델 바꾸면? 재색인!
    private float[] embedding;

    public static ProductDocument from(Product p) {
        return new ProductDocument(p.getId(), p.getSellerId(), p.getName(), p.getDescription(), p.getPrice(), p.getStatus().name(), p.getSalesCount()
        , null, null, null, null, false, null);
    }

    public static ProductDocument from(Product p, ProductAttributes a, float[] embedding) {
        ProductDocument doc = from(p);
        if (a != null) {
            doc.occasions = a.occasions();
            doc.styles = a.styles();
            doc.seasons = a.seasons();
            doc.material = a.material();
            doc.rainFriendly = a.rainFriendly();
        }
        doc.embedding = embedding;
        return doc;
    }
}
