package procureai.matching.service;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import procureai.matching.client.*;
import procureai.matching.dto.MatchResultResponse;
import procureai.matching.entity.Discrepancy;
import procureai.matching.entity.MatchResult;
import procureai.matching.repository.MatchResultRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MatchingServiceImpl implements MatchingService {

    private final MatchResultRepository repository;
    private final InvoiceClient invoiceClient;
    private final OrderClient orderClient;
    private final ReceptionClient receptionClient;
    private final BigDecimal priceTolerancePercent;

    public MatchingServiceImpl(MatchResultRepository repository,
                               InvoiceClient invoiceClient,
                               OrderClient orderClient,
                               ReceptionClient receptionClient,
                               @Value("${matching.price-tolerance-percent:0}") BigDecimal priceTolerancePercent) {
        this.repository = repository;
        this.invoiceClient = invoiceClient;
        this.orderClient = orderClient;
        this.receptionClient = receptionClient;
        this.priceTolerancePercent = priceTolerancePercent;
    }

    // ---------------------------------------------------------------
    // Rapprochement : facture <-> commande <-> receptions
    // ---------------------------------------------------------------

    @Override
    public MatchResultResponse matchInvoice(Long invoiceId) {
        InvoiceResponse invoice = findInvoice(invoiceId);
        if (!"RECEIVED".equals(invoice.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only RECEIVED invoices can be matched, current status: " + invoice.status());
        }
        OrderResponse order = findOrder(invoice.purchaseOrderId());

        MatchResult result = new MatchResult();
        result.setInvoiceId(invoice.id());
        result.setPurchaseOrderId(order.id());

        checkSupplier(invoice, order, result);
        checkProductsAndPrices(invoice, order, result);
        checkQuantities(invoice, order, result);

        boolean matched = result.getDiscrepancies().isEmpty();
        result.setStatus(matched ? MatchResult.Status.MATCHED : MatchResult.Status.MISMATCH);
        MatchResult saved = repository.save(result);

        // Appele apres la sauvegarde : si invoice-service echoue, la transaction annule tout
        if (matched) {
            invoiceClient.validateInvoice(invoice.id());
        } else {
            invoiceClient.rejectInvoice(invoice.id());
        }
        return MatchResultResponse.from(saved);
    }

    // La facture doit venir du fournisseur de la commande
    private void checkSupplier(InvoiceResponse invoice, OrderResponse order, MatchResult result) {
        if (!order.supplierId().equals(invoice.supplierId())) {
            result.addDiscrepancy(new Discrepancy(Discrepancy.Type.SUPPLIER_MISMATCH, null,
                    String.valueOf(order.supplierId()), String.valueOf(invoice.supplierId())));
        }
    }

    // Chaque produit facture doit avoir ete commande, au meme prix
    private void checkProductsAndPrices(InvoiceResponse invoice, OrderResponse order, MatchResult result) {
        for (InvoiceResponse.Line line : invoice.lines()) {
            BigDecimal orderedPrice = findOrderedPrice(order, line.productId());

            if (orderedPrice == null) {
                result.addDiscrepancy(new Discrepancy(Discrepancy.Type.PRODUCT_NOT_ORDERED, line.productId(),
                        null, String.valueOf(line.quantity())));
            } else if (!isPriceAccepted(orderedPrice, line.unitPrice())) {
                result.addDiscrepancy(new Discrepancy(Discrepancy.Type.PRICE_MISMATCH, line.productId(),
                        orderedPrice.toPlainString(), line.unitPrice().toPlainString()));
            }
        }
    }

    // On ne paie que ce qui a ete recu, et jamais deux fois
    private void checkQuantities(InvoiceResponse invoice, OrderResponse order, MatchResult result) {
        Map<Long, Integer> received = getAcceptedQuantities(order.id());
        Map<Long, Integer> alreadyInvoiced = getAlreadyInvoicedQuantities(order.id(), invoice.id());
        Map<Long, Integer> invoiced = getInvoicedQuantities(invoice);

        for (Long productId : invoiced.keySet()) {
            if (findOrderedPrice(order, productId) == null) {
                continue;
            }
            int available = received.getOrDefault(productId, 0) - alreadyInvoiced.getOrDefault(productId, 0);
            int quantity = invoiced.get(productId);

            if (quantity > available) {
                result.addDiscrepancy(new Discrepancy(Discrepancy.Type.QUANTITY_EXCEEDS_RECEIVED, productId,
                        String.valueOf(available), String.valueOf(quantity)));
            }
        }
    }

    // ---------------------------------------------------------------
    // Petites methodes d'aide
    // ---------------------------------------------------------------

    private InvoiceResponse findInvoice(Long invoiceId) {
        try {
            return invoiceClient.getInvoiceById(invoiceId);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found");
        }
    }

    private OrderResponse findOrder(Long orderId) {
        try {
            return orderClient.getPurchaseOrderById(orderId);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found");
        }
    }

    private BigDecimal findOrderedPrice(OrderResponse order, Long productId) {
        for (OrderResponse.Line line : order.lines()) {
            if (line.productId().equals(productId)) {
                return line.unitPrice();
            }
        }
        return null;
    }

    private boolean isPriceAccepted(BigDecimal orderedPrice, BigDecimal invoicedPrice) {
        BigDecimal difference = invoicedPrice.subtract(orderedPrice).abs();
        BigDecimal maxDifference = orderedPrice.multiply(priceTolerancePercent).divide(BigDecimal.valueOf(100));
        return difference.compareTo(maxDifference) <= 0;
    }

    // Quantite recue et acceptee (recue - rejetee), par produit, sur toutes les receptions
    private Map<Long, Integer> getAcceptedQuantities(Long orderId) {
        Map<Long, Integer> quantities = new HashMap<>();
        for (ReceptionResponse reception : receptionClient.getReceptionsByOrder(orderId)) {
            for (ReceptionResponse.Line line : reception.lines()) {
                addQuantity(quantities, line.productId(), line.acceptedQuantity());
            }
        }
        return quantities;
    }

    // Quantite deja facturee par d'autres factures VALIDATED ou PAID de la meme commande
    private Map<Long, Integer> getAlreadyInvoicedQuantities(Long orderId, Long currentInvoiceId) {
        Map<Long, Integer> quantities = new HashMap<>();
        for (InvoiceResponse other : invoiceClient.getInvoicesByOrder(orderId)) {
            boolean isCurrentInvoice = other.id().equals(currentInvoiceId);
            boolean isAccepted = "VALIDATED".equals(other.status()) || "PAID".equals(other.status());
            if (isCurrentInvoice || !isAccepted) {
                continue;
            }
            for (InvoiceResponse.Line line : other.lines()) {
                addQuantity(quantities, line.productId(), line.quantity());
            }
        }
        return quantities;
    }

    private Map<Long, Integer> getInvoicedQuantities(InvoiceResponse invoice) {
        Map<Long, Integer> quantities = new HashMap<>();
        for (InvoiceResponse.Line line : invoice.lines()) {
            addQuantity(quantities, line.productId(), line.quantity());
        }
        return quantities;
    }

    private void addQuantity(Map<Long, Integer> quantities, Long productId, int quantity) {
        quantities.put(productId, quantities.getOrDefault(productId, 0) + quantity);
    }

    // ---------------------------------------------------------------
    // Lecture des resultats
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public MatchResultResponse getMatchResult(Long id) {
        return repository.findById(id)
                .map(MatchResultResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match result not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchResultResponse> getAllMatchResults() {
        return toResponses(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchResultResponse> getMatchResultsByInvoice(Long invoiceId) {
        return toResponses(repository.findByInvoiceIdOrderByCheckedAtDesc(invoiceId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchResultResponse> getMatchResultsByStatus(MatchResult.Status status) {
        return toResponses(repository.findByStatus(status));
    }

    private List<MatchResultResponse> toResponses(List<MatchResult> results) {
        return results.stream().map(MatchResultResponse::from).toList();
    }
}
