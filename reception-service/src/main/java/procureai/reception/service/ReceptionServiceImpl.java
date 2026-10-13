package procureai.reception.service;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import procureai.reception.client.OrderClient;
import procureai.reception.client.PurchaseOrderResponse;
import procureai.reception.dto.ReceptionRequest;
import procureai.reception.dto.ReceptionResponse;
import procureai.reception.entity.Reception;
import procureai.reception.entity.ReceptionLine;
import procureai.reception.repository.ReceptionRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReceptionServiceImpl implements ReceptionService {

    private static final Set<String> RECEIVABLE_ORDER_STATUSES = Set.of("SENT", "CONFIRMED", "DELIVERED");

    private final ReceptionRepository repository;
    private final OrderClient orderClient;

    public ReceptionServiceImpl(ReceptionRepository repository, OrderClient orderClient) {
        this.repository = repository;
        this.orderClient = orderClient;
    }

    @Override
    public ReceptionResponse createReception(ReceptionRequest request) {
        PurchaseOrderResponse order;
        try {
            order = orderClient.getPurchaseOrderById(request.purchaseOrderId());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found");
        }
        if (!order.supplierId().equals(request.supplierId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplier does not match the purchase order");
        }
        if (!RECEIVABLE_ORDER_STATUSES.contains(order.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Purchase order cannot be received, current status: " + order.status());
        }

        Map<Long, Integer> orderedQuantities = order.lines().stream()
                .collect(Collectors.toMap(PurchaseOrderResponse.Line::productId, PurchaseOrderResponse.Line::quantity, Integer::sum));

        Reception reception = new Reception();
        reception.setPurchaseOrderId(request.purchaseOrderId());
        reception.setSupplierId(request.supplierId());
        reception.setDeliveryNoteNumber(request.deliveryNoteNumber());
        reception.setComment(request.comment());
        if (request.receivedDate() != null) {
            reception.setReceivedDate(request.receivedDate());
        }

        for (ReceptionRequest.Line requestLine : request.lines()) {
            Integer ordered = orderedQuantities.get(requestLine.productId());
            if (ordered == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product " + requestLine.productId() + " is not in the purchase order");
            }
            if (requestLine.quantityReceived() > ordered) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Received quantity cannot exceed ordered quantity");
            }
            if (requestLine.quantityRejected() > requestLine.quantityReceived()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejected quantity cannot exceed received quantity");
            }

            ReceptionLine line = new ReceptionLine();
            line.setProductId(requestLine.productId());
            line.setProductName(requestLine.productName());
            line.setQuantityOrdered(ordered);
            line.setQuantityReceived(requestLine.quantityReceived());
            line.setQuantityRejected(requestLine.quantityRejected());
            line.setReception(reception);
            reception.getLines().add(line);
        }
        return ReceptionResponse.from(repository.save(reception));
    }

    @Override
    @Transactional(readOnly = true)
    public ReceptionResponse getReceptionById(Long id) {
        return repository.findById(id)
                .map(ReceptionResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reception not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceptionResponse> getAllReceptions() {
        return repository.findAll().stream().map(ReceptionResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceptionResponse> getReceptionsByOrder(Long purchaseOrderId) {
        return repository.findByPurchaseOrderId(purchaseOrderId).stream().map(ReceptionResponse::from).toList();
    }
}
