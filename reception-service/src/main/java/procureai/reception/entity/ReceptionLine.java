package procureai.reception.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reception_lines")
public class ReceptionLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private String productName;

    private int quantityOrdered;

    private int quantityReceived;

    private int quantityRejected;

    @ManyToOne
    @JoinColumn(name = "reception_id", nullable = false)
    private Reception reception;

    public ReceptionLine() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(int quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public int getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(int quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public int getQuantityRejected() {
        return quantityRejected;
    }

    public void setQuantityRejected(int quantityRejected) {
        this.quantityRejected = quantityRejected;
    }

    public Reception getReception() {
        return reception;
    }

    public void setReception(Reception reception) {
        this.reception = reception;
    }
}
