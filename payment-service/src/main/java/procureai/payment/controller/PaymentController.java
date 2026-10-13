package procureai.payment.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import procureai.payment.client.InvoiceResponse;
import procureai.payment.dto.CreatePaymentRequest;
import procureai.payment.dto.PaymentResponse;
import procureai.payment.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    // Seul le comptable paie : l'admin voit les paiements mais ne peut pas en faire
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse payInvoice(@Valid @RequestBody CreatePaymentRequest request,
                                      @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "COMPTABLE");
        return service.payInvoice(request);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable Long id) {
        return service.getPayment(id);
    }

    @GetMapping
    public List<PaymentResponse> getAllPayments() {
        return service.getAllPayments();
    }

    @GetMapping("/invoice/{invoiceId}")
    public PaymentResponse getPaymentByInvoice(@PathVariable Long invoiceId) {
        return service.getPaymentByInvoice(invoiceId);
    }

    @GetMapping("/supplier/{supplierId}")
    public List<PaymentResponse> getPaymentsBySupplier(@PathVariable Long supplierId) {
        return service.getPaymentsBySupplier(supplierId);
    }

    @GetMapping("/to-pay")
    public List<InvoiceResponse> getInvoicesToPay() {
        return service.getInvoicesToPay();
    }

    // Le role vient du JWT : le gateway le met dans le header X-User-Role
    private void checkRole(String userRole, String... allowedRoles) {
        if (userRole == null || !List.of(allowedRoles).contains(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role: " + userRole);
        }
    }
}
