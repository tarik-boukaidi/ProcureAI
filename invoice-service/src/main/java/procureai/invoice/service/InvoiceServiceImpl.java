package procureai.invoice.service;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import procureai.invoice.client.AccountingClient;
import procureai.invoice.client.AccountingEntryRequest;
import procureai.invoice.client.OrderClient;
import procureai.invoice.client.PurchaseOrderResponse;
import procureai.invoice.dto.InvoiceRequest;
import procureai.invoice.dto.InvoiceResponse;
import procureai.invoice.entity.Invoice;
import procureai.invoice.entity.InvoiceLine;
import procureai.invoice.messaging.NotificationPublisher;
import procureai.invoice.messaging.NotificationRequest;
import procureai.invoice.repository.InvoiceRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private static final int PAYMENT_TERM_DAYS = 30;
    private static final Set<String> INVOICEABLE_ORDER_STATUSES = Set.of("SENT", "CONFIRMED", "DELIVERED");
    // Taux de TVA marocains
    private static final List<BigDecimal> VAT_RATES = Stream.of(0, 7, 10, 14, 20).map(BigDecimal::valueOf).toList();
    private static final BigDecimal DEFAULT_VAT_RATE = BigDecimal.valueOf(20);

    private static final Logger log = LoggerFactory.getLogger(InvoiceServiceImpl.class);

    private final InvoiceRepository repository;
    private final OrderClient orderClient;
    private final NotificationPublisher notificationPublisher;
    private final AccountingClient accountingClient;

    public InvoiceServiceImpl(InvoiceRepository repository, OrderClient orderClient,
                              NotificationPublisher notificationPublisher, AccountingClient accountingClient) {
        this.repository = repository;
        this.orderClient = orderClient;
        this.notificationPublisher = notificationPublisher;
        this.accountingClient = accountingClient;
    }

    @Override
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        PurchaseOrderResponse order;
        try {
            order = orderClient.getPurchaseOrderById(request.purchaseOrderId());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found");
        }
        if (!order.supplierId().equals(request.supplierId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplier does not match the purchase order");
        }
        if (!INVOICEABLE_ORDER_STATUSES.contains(order.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Purchase order cannot be invoiced, current status: " + order.status());
        }
        if (repository.existsBySupplierIdAndInvoiceNumber(request.supplierId(), request.invoiceNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice number already exists for this supplier");
        }

        BigDecimal vatRate = request.vatRate() == null ? DEFAULT_VAT_RATE : request.vatRate();
        if (VAT_RATES.stream().noneMatch(rate -> rate.compareTo(vatRate) == 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VAT rate must be 0, 7, 10, 14 or 20");
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(request.invoiceNumber());
        invoice.setPurchaseOrderId(request.purchaseOrderId());
        invoice.setSupplierId(request.supplierId());
        if (request.invoiceDate() != null) {
            invoice.setInvoiceDate(request.invoiceDate());
        }
        invoice.setDueDate(request.dueDate() != null
                ? request.dueDate()
                : invoice.getInvoiceDate().plusDays(PAYMENT_TERM_DAYS));
        invoice.setVatRate(vatRate);

        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceRequest.Line requestLine : request.lines()) {
            InvoiceLine line = new InvoiceLine();
            line.setProductId(requestLine.productId());
            line.setProductName(requestLine.productName());
            line.setQuantity(requestLine.quantity());
            line.setUnitPrice(requestLine.unitPrice());
            line.setInvoice(invoice);
            invoice.getLines().add(line);
            total = total.add(line.getLineTotal());
        }
        BigDecimal vat = total.multiply(vatRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        invoice.setTotalAmount(total);
        invoice.setVatAmount(vat);
        invoice.setTotalTtc(total.add(vat));

        return InvoiceResponse.from(repository.save(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        return InvoiceResponse.from(findInvoice(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return toResponses(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByPurchaseOrder(Long purchaseOrderId) {
        return toResponses(repository.findByPurchaseOrderId(purchaseOrderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesBySupplier(Long supplierId) {
        return toResponses(repository.findBySupplierId(supplierId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByStatus(Invoice.Status status) {
        return toResponses(repository.findByStatus(status));
    }

    @Override
    public InvoiceResponse validateInvoice(Long id) {
        Invoice invoice = changeStatus(id, Invoice.Status.RECEIVED, Invoice.Status.VALIDATED);
        // La dette fournisseur (TTC) = l'achat (HT) + la TVA que l'Etat nous rendra
        recordInAccounting(invoice, "INVOICE_RECORDED", invoice.getTotalAmount(),
                "Facture " + invoice.getInvoiceNumber() + " a payer");
        if (invoice.getVatAmount().signum() > 0) {
            recordInAccounting(invoice, "VAT_RECORDED", invoice.getVatAmount(),
                    "TVA facture " + invoice.getInvoiceNumber());
        }
        return InvoiceResponse.from(invoice);
    }

    @Override
    public InvoiceResponse rejectInvoice(Long id) {
        Invoice invoice = changeStatus(id, Invoice.Status.RECEIVED, Invoice.Status.REJECTED);
        notify(new NotificationRequest(null, "COMPTABLE", "INVOICE_REJECTED",
                "Facture " + invoice.getInvoiceNumber() + " rejetee"));
        return InvoiceResponse.from(invoice);
    }

    @Override
    public InvoiceResponse markAsPaid(Long id) {
        Invoice invoice = changeStatus(id, Invoice.Status.VALIDATED, Invoice.Status.PAID);
        recordInAccounting(invoice, "PAYMENT_RECORDED", invoice.getTotalTtc(),
                "Paiement facture " + invoice.getInvoiceNumber());
        notify(new NotificationRequest(null, "COMPTABLE", "INVOICE_PAID",
                "Facture " + invoice.getInvoiceNumber() + " payee : " + invoice.getTotalTtc()));
        return InvoiceResponse.from(invoice);
    }

    private Invoice findInvoice(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
    }

    private List<InvoiceResponse> toResponses(List<Invoice> invoices) {
        return invoices.stream().map(InvoiceResponse::from).toList();
    }

    private Invoice changeStatus(Long id, Invoice.Status expected, Invoice.Status newStatus) {
        Invoice invoice = findInvoice(id);
        if (invoice.getStatus() != expected) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Invoice must be " + expected + ", current status: " + invoice.getStatus());
        }
        invoice.setStatus(newStatus);
        return repository.save(invoice);
    }

    // Contrairement aux notifications, la comptabilite est obligatoire :
    // si accounting-service echoue, l'exception annule le changement de statut (@Transactional)
    private void recordInAccounting(Invoice invoice, String type, BigDecimal amount, String description) {
        try {
            accountingClient.record(new AccountingEntryRequest(
                    type, invoice.getId(), invoice.getSupplierId(), amount, description));
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Accounting service unavailable");
        }
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
