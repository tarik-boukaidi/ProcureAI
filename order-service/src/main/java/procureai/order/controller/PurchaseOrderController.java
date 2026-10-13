package procureai.order.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import procureai.order.dto.PurchaseOrderRequest;
import procureai.order.dto.PurchaseOrderResponse;
import procureai.order.entity.PurchaseOrder;
import procureai.order.service.PurchaseService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class PurchaseOrderController {

    private final PurchaseService service;

    public PurchaseOrderController(PurchaseService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse createPurchaseOrder(@Valid @RequestBody PurchaseOrderRequest request) {
        return service.createPurchaseOrder(request);
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse getPurchaseOrder(@PathVariable Long id) {
        return service.getPurchaseOrder(id);
    }

    @GetMapping
    public List<PurchaseOrderResponse> getAllPurchaseOrders() {
        return service.getAllPurchaseOrders();
    }

    @GetMapping("/supplier/{supplierId}")
    public List<PurchaseOrderResponse> getPurchaseOrdersBySupplier(@PathVariable Long supplierId) {
        return service.getPurchaseOrdersBySupplierId(supplierId);
    }

    @GetMapping("/status/{status}")
    public List<PurchaseOrderResponse> getPurchaseOrdersByStatus(@PathVariable PurchaseOrder.Status status) {
        return service.getPurchaseOrdersByStatus(status);
    }

    @GetMapping("/purchase-request/{purchaseRequestId}")
    public List<PurchaseOrderResponse> getPurchaseOrdersByPurchaseRequest(@PathVariable Long purchaseRequestId) {
        return service.getPurchaseOrdersByPurchaseRequestId(purchaseRequestId);
    }

    @GetMapping("/number/{orderNumber}")
    public PurchaseOrderResponse getPurchaseOrderByOrderNumber(@PathVariable String orderNumber) {
        return service.getPurchaseOrderByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
    }

    // Ex : PUT /api/orders/3/status?status=SENT. L'admin suit la commande avec le fournisseur.
    @PutMapping("/{id}/status")
    public PurchaseOrderResponse changeStatus(@PathVariable Long id,
                                              @RequestParam PurchaseOrder.Status status,
                                              @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "ADMIN");
        return service.changeStatus(id, status);
    }

    // Le role vient du JWT : le gateway le met dans le header X-User-Role
    private void checkRole(String userRole, String... allowedRoles) {
        if (userRole == null || !List.of(allowedRoles).contains(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role: " + userRole);
        }
    }
}
