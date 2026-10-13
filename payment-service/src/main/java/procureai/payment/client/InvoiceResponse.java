package procureai.payment.client;

import java.math.BigDecimal;
import java.time.LocalDate;

// totalAmount = HT, totalTtc = ce qu'on paie au fournisseur
public record InvoiceResponse(Long id, String invoiceNumber, Long supplierId, String status,
                              BigDecimal totalAmount, BigDecimal totalTtc, LocalDate dueDate) {
}
