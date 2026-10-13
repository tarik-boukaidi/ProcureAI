package procureai.matching.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "match_results")
public class MatchResult {

    public enum Status {
        MATCHED,
        MISMATCH
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long invoiceId;

    @Column(nullable = false)
    private Long purchaseOrderId;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime checkedAt;

    @OneToMany(
            mappedBy = "matchResult",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Discrepancy> discrepancies = new ArrayList<>();

    public MatchResult() {
        this.checkedAt = LocalDateTime.now();
    }

    public void addDiscrepancy(Discrepancy discrepancy) {
        discrepancy.setMatchResult(this);
        discrepancies.add(discrepancy);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    public List<Discrepancy> getDiscrepancies() {
        return discrepancies;
    }

    public void setDiscrepancies(List<Discrepancy> discrepancies) {
        this.discrepancies = discrepancies;
    }
}
