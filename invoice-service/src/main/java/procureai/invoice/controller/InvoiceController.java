package procureai.invoice.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import procureai.invoice.dto.InvoiceRequest;
import procureai.invoice.dto.InvoiceResponse;
import procureai.invoice.entity.Invoice;
import procureai.invoice.service.InvoiceService;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    // Le comptable saisit la facture envoyee par le fournisseur
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInvoice(@Valid @RequestBody InvoiceRequest request,
                                         @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "COMPTABLE");
        return service.createInvoice(request);
    }

    @GetMapping("/{id}")
    public InvoiceResponse getInvoice(@PathVariable Long id) {
        return service.getInvoiceById(id);
    }

    @GetMapping
    public List<InvoiceResponse> getAllInvoices() {
        return service.getAllInvoices();
    }

    @GetMapping("/order/{purchaseOrderId}")
    public List<InvoiceResponse> getInvoicesByPurchaseOrder(@PathVariable Long purchaseOrderId) {
        return service.getInvoicesByPurchaseOrder(purchaseOrderId);
    }

    @GetMapping("/supplier/{supplierId}")
    public List<InvoiceResponse> getInvoicesBySupplier(@PathVariable Long supplierId) {
        return service.getInvoicesBySupplier(supplierId);
    }

    @GetMapping("/status/{status}")
    public List<InvoiceResponse> getInvoicesByStatus(@PathVariable Invoice.Status status) {
        return service.getInvoicesByStatus(status);
    }

    // Appele par matching-service apres le rapprochement
    @PutMapping("/{id}/validate")
    public InvoiceResponse validateInvoice(@PathVariable Long id,
                                           @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkInternalCall(role);
        return service.validateInvoice(id);
    }

    // Appele par matching-service apres le rapprochement
    @PutMapping("/{id}/reject")
    public InvoiceResponse rejectInvoice(@PathVariable Long id,
                                         @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkInternalCall(role);
        return service.rejectInvoice(id);
    }

    // Appele par payment-service apres le paiement
    @PutMapping("/{id}/pay")
    public InvoiceResponse markAsPaid(@PathVariable Long id,
                                      @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkInternalCall(role);
        return service.markAsPaid(id);
    }

    // Le role vient du JWT : le gateway le met dans le header X-User-Role
    private void checkRole(String userRole, String... allowedRoles) {
        if (userRole == null || !List.of(allowedRoles).contains(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role: " + userRole);
        }
    }

    // Le gateway ajoute toujours X-User-Role : une requete qui l'a vient de l'exterieur.
    // Le statut d'une facture ne change que par le rapprochement ou le paiement.
    private void checkInternalCall(String userRole) {
        if (userRole != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Invoice status is changed by matching-service or payment-service only");
        }
    }
}
