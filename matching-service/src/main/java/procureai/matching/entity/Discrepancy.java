package procureai.matching.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "match_discrepancies")
public class Discrepancy {

    public enum Type {
        SUPPLIER_MISMATCH,
        PRODUCT_NOT_ORDERED,
        PRICE_MISMATCH,
        QUANTITY_EXCEEDS_RECEIVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Type type;

    private Long productId;

    private String expectedValue;

    private String actualValue;

    @ManyToOne
    @JoinColumn(name = "match_result_id", nullable = false)
    private MatchResult matchResult;

    public Discrepancy() {
    }

    public Discrepancy(Type type, Long productId, String expectedValue, String actualValue) {
        this.type = type;
        this.productId = productId;
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    public void setMatchResult(MatchResult matchResult) {
        this.matchResult = matchResult;
    }
}
