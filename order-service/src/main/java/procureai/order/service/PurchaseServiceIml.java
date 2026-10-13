package procureai.order.service;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import procureai.order.client.ProcurementClient;
import procureai.order.client.PurchaseRequestResponse;
import procureai.order.dto.PurchaseOrderRequest;
import procureai.order.dto.PurchaseOrderResponse;
import procureai.order.entity.PurchaseOrder;
import procureai.order.entity.PurchaseOrder.Status;
import procureai.order.entity.PurchaseOrderLine;
import procureai.order.messaging.NotificationPublisher;
import procureai.order.messaging.NotificationRequest;
import procureai.order.repository.PurchaseOrderRepository;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class PurchaseServiceIml implements PurchaseService {

    // Cycle de vie d'une commande : statut actuel -> statuts suivants autorises
    private static final Map<Status, Set<Status>> NEXT_STATUSES = Map.of(
            Status.DRAFT, Set.of(Status.SENT, Status.CANCELLED),
            Status.SENT, Set.of(Status.CONFIRMED, Status.DELIVERED, Status.CANCELLED),
            Status.CONFIRMED, Set.of(Status.DELIVERED, Status.CANCELLED),
            Status.DELIVERED, Set.of(),
            Status.CANCELLED, Set.of()
    );

    private static final Logger log = LoggerFactory.getLogger(PurchaseServiceIml.class);

    private final PurchaseOrderRepository repository;
    private final ProcurementClient procurementClient;
    private final NotificationPublisher notificationPublisher;

    public PurchaseServiceIml(PurchaseOrderRepository repository, ProcurementClient procurementClient,
                              NotificationPublisher notificationPublisher) {
        this.repository = repository;
        this.procurementClient = procurementClient;
        this.notificationPublisher = notificationPublisher;
    }

    @Override
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        PurchaseRequestResponse demande;
        try {
            demande = procurementClient.getDemandeById(request.purchaseRequestId());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase request not found");
        }
        if (!"APPROVED".equals(demande.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Purchase request must be APPROVED");
        }

        PurchaseOrder order = new PurchaseOrder();
        if (request.orderNumber() == null || request.orderNumber().isBlank()) {
            order.setOrderNumber("PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } else if (repository.existsByOrderNumber(request.orderNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order number already exists");
        } else {
            order.setOrderNumber(request.orderNumber());
        }
        order.setPurchaseRequestId(request.purchaseRequestId());
        order.setSupplierId(request.supplierId());
        order.setExpectedDeliveryDate(request.expectedDeliveryDate());

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderRequest.Line requestLine : request.lines()) {
            PurchaseOrderLine line = new PurchaseOrderLine();
            line.setProductId(requestLine.productId());
            line.setProductName(requestLine.productName());
            line.setQuantity(requestLine.quantity());
            line.setUnitPrice(requestLine.unitPrice());
            order.addLine(line);
            total = total.add(line.getLineTotal());
        }
        order.setTotalAmount(total);

        return PurchaseOrderResponse.from(repository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrder(Long id) {
        return PurchaseOrderResponse.from(findOrder(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllPurchaseOrders() {
        return toResponses(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPurchaseOrdersBySupplierId(Long supplierId) {
        return toResponses(repository.findBySupplierId(supplierId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPurchaseOrdersByStatus(PurchaseOrder.Status status) {
        return toResponses(repository.findByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPurchaseOrdersByPurchaseRequestId(Long purchaseRequestId) {
        return toResponses(repository.findByPurchaseRequestId(purchaseRequestId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PurchaseOrderResponse> getPurchaseOrderByOrderNumber(String orderNumber) {
        return repository.findByOrderNumber(orderNumber).map(PurchaseOrderResponse::from);
    }

    @Override
    public PurchaseOrderResponse changeStatus(Long id, Status status) {
        PurchaseOrder order = findOrder(id);
        if (!NEXT_STATUSES.get(order.getStatus()).contains(status)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot change order status from " + order.getStatus() + " to " + status);
        }
        order.setStatus(status);
        PurchaseOrder saved = repository.save(order);

        // Commande envoyee au fournisseur : le magasinier sait qu'une livraison arrive
        if (status == Status.SENT) {
            String message = "Commande " + order.getOrderNumber() + " envoyee, a receptionner";
            if (order.getExpectedDeliveryDate() != null) {
                message += " le " + order.getExpectedDeliveryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            notify(new NotificationRequest(null, "MAGASINIER", "ORDER_SENT", message));
        }
        return PurchaseOrderResponse.from(saved);
    }

    private PurchaseOrder findOrder(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
    }

    private List<PurchaseOrderResponse> toResponses(List<PurchaseOrder> orders) {
        return orders.stream().map(PurchaseOrderResponse::from).toList();
    }

    // Une notification ratee ne doit pas annuler le changement de statut : on log l'erreur et on continue
    private void notify(NotificationRequest notification) {
        try {
            notificationPublisher.send(notification);
        } catch (Exception e) {
            log.warn("Notification not sent: {}", e.getMessage());
        }
    }
}