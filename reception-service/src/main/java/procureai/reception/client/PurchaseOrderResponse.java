package procureai.reception.client;

import java.util.List;

public record PurchaseOrderResponse(Long id, Long supplierId, String status, List<Line> lines) {

    public record Line(Long productId, String productName, int quantity) {
    }
}
