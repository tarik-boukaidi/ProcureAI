package procureai.invoice.client;

import java.math.BigDecimal;

public record PurchaseOrderResponse(Long id, String orderNumber, Long supplierId, String status, BigDecimal totalAmount) {
}
