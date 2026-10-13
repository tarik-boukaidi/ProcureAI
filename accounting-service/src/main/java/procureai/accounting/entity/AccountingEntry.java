package procureai.accounting.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// Une ecriture comptable n'est jamais modifiee ni supprimee
@Entity
@Table(name = "accounting_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"type", "invoice_id"}))
public class AccountingEntry {

    // Facture validee = INVOICE_RECORDED (HT) + VAT_RECORDED (TVA). Paiement = PAYMENT_RECORDED (TTC).
    public enum Type {
        INVOICE_RECORDED,
        VAT_RECORDED,
        PAYMENT_RECORDED
    }

    // Plan comptable marocain (CGNC)
    public static final String PURCHASES_ACCOUNT = "6111"; // Achats de marchandises
    public static final String VAT_ACCOUNT = "3455";       // Etat - TVA recuperable
    public static final String SUPPLIERS_ACCOUNT = "4411"; // Fournisseurs
    public static final String BANK_ACCOUNT = "5141";      // Banques

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(nullable = false)
    private Long supplierId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String debitAccount;

    @Column(nullable = false)
    private String creditAccount;

    private String description;

    @Column(nullable = false)
    private LocalDate entryDate;

    public AccountingEntry() {
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

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDebitAccount() {
        return debitAccount;
    }

    public void setDebitAccount(String debitAccount) {
        this.debitAccount = debitAccount;
    }

    public String getCreditAccount() {
        return creditAccount;
    }

    public void setCreditAccount(String creditAccount) {
        this.creditAccount = creditAccount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }
}
