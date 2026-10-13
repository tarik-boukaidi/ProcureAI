package procureai.order.dto;

import procureai.order.entity.PurchaseOrder;
import procureai.order.entity.PurchaseOrder.Status;
import procureai.order.entity.PurchaseOrderLine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderResponse(
        Long id,
        String orderNumber,
        Long purchaseRequestId,
        Long supplierId,
        LocalDate orderDate,
        LocalDate expectedDeliveryDate,
        BigDecimal totalAmount,
        Status status,
        List<Line> lines) {

    public record Line(Long id, Long productId, String productName, int quantity, BigDecimal unitPrice) {

        static Line from(PurchaseOrderLine line) {
            return new Line(line.getId(), line.getProductId(), line.getProductName(), line.getQuantity(), line.getUnitPrice());
        }
    }

    public static PurchaseOrderResponse from(PurchaseOrder order) {
        return new PurchaseOrderResponse(order.getId(), order.getOrderNumber(), order.getPurchaseRequestId(),
                order.getSupplierId(), order.getOrderDate(), order.getExpectedDeliveryDate(), order.getTotalAmount(),
                order.getStatus(), order.getLines().stream().map(Line::from).toList());
    }
}
