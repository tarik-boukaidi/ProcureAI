package procureai.demande.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_requests")
public class PurchaseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reason;

    private Long requesterId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(
            mappedBy = "purchaseRequest",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PurchaseRequestLine> items = new ArrayList<>();

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    public PurchaseRequest() {
        this.status = Status.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<PurchaseRequestLine> getItems() {
        return items;
    }

    public void setItems(List<PurchaseRequestLine> items) {
        this.items = items;
    }
}