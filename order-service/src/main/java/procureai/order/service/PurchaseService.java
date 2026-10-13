package procureai.order.service;

import procureai.order.dto.PurchaseOrderRequest;
import procureai.order.dto.PurchaseOrderResponse;
import procureai.order.entity.PurchaseOrder;

import java.util.List;
import java.util.Optional;

public interface PurchaseService {
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request);
    PurchaseOrderResponse getPurchaseOrder(Long id);
    List<PurchaseOrderResponse> getAllPurchaseOrders();
    List<PurchaseOrderResponse> getPurchaseOrdersBySupplierId(Long supplierId);
    List<PurchaseOrderResponse> getPurchaseOrdersByStatus(PurchaseOrder.Status status);
    List<PurchaseOrderResponse> getPurchaseOrdersByPurchaseRequestId(Long purchaseRequestId);
    Optional<PurchaseOrderResponse> getPurchaseOrderByOrderNumber(String orderNumber);
    PurchaseOrderResponse changeStatus(Long id, PurchaseOrder.Status status);
}
