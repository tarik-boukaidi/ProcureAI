package procureai.invoice.client;

import java.math.BigDecimal;

// type : INVOICE_RECORDED ou PAYMENT_RECORDED (les comptes sont choisis par accounting-service)
public record AccountingEntryRequest(String type, Long invoiceId, Long supplierId, BigDecimal amount, String description) {
}
