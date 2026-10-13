package procureai.payment.service;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import procureai.payment.client.InvoiceClient;
import procureai.payment.client.InvoiceResponse;
import procureai.payment.dto.CreatePaymentRequest;
import procureai.payment.dto.PaymentResponse;
import procureai.payment.entity.Payment;
import procureai.payment.repository.PaymentRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final InvoiceClient invoiceClient;

    public PaymentServiceImpl(PaymentRepository repository, InvoiceClient invoiceClient) {
        this.repository = repository;
        this.invoiceClient = invoiceClient;
    }

    @Override
    public PaymentResponse payInvoice(CreatePaymentRequest request) {
        InvoiceResponse invoice = findInvoice(request.invoiceId());

        // Seule une facture validee par le rapprochement peut etre payee
        if (!"VALIDATED".equals(invoice.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only VALIDATED invoices can be paid, current status: " + invoice.status());
        }
        if (repository.existsByInvoiceId(invoice.id())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice already paid");
        }

        Payment payment = new Payment();
        payment.setReference("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setInvoiceId(invoice.id());
        payment.setSupplierId(invoice.supplierId());
        payment.setAmount(invoice.totalTtc());
        payment.setMethod(request.method());
        payment.setPaymentDate(request.paymentDate() != null ? request.paymentDate() : LocalDate.now());

        Payment saved = repository.save(payment);

        // Appele apres la sauvegarde : si invoice-service echoue, la transaction annule tout
        invoiceClient.markAsPaid(invoice.id());
        return PaymentResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {
        return repository.findById(id)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return repository.findAll().stream().map(PaymentResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByInvoice(Long invoiceId) {
        return repository.findByInvoiceId(invoiceId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No payment for this invoice"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsBySupplier(Long supplierId) {
        return repository.findBySupplierId(supplierId).stream().map(PaymentResponse::from).toList();
    }

    // Factures validees pas encore payees, la plus urgente en premier
    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesToPay() {
        List<InvoiceResponse> invoices = invoiceClient.getInvoicesByStatus("VALIDATED");
        return invoices.stream()
                .sorted(Comparator.comparing(InvoiceResponse::dueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private InvoiceResponse findInvoice(Long invoiceId) {
        try {
            return invoiceClient.getInvoiceById(invoiceId);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found");
        }
    }
}
