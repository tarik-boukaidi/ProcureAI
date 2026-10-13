package procureai.invoice.dto;

import procureai.invoice.entity.Invoice;
import procureai.invoice.entity.InvoiceLine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        Long purchaseOrderId,
        Long supplierId,
        LocalDate invoiceDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BigDecimal vatRate,
        BigDecimal vatAmount,
        BigDecimal totalTtc,
        Invoice.Status status,
        List<Line> lines) {

    public record Line(Long id, Long productId, String productName, int quantity, BigDecimal unitPrice) {

        static Line from(InvoiceLine line) {
            return new Line(line.getId(), line.getProductId(), line.getProductName(),
                    line.getQuantity(), line.getUnitPrice());
        }
    }

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(invoice.getId(), invoice.getInvoiceNumber(), invoice.getPurchaseOrderId(),
                invoice.getSupplierId(), invoice.getInvoiceDate(), invoice.getDueDate(), invoice.getTotalAmount(),
                invoice.getVatRate(), invoice.getVatAmount(), invoice.getTotalTtc(), invoice.getStatus(),
                invoice.getLines().stream().map(Line::from).toList());
    }
}
