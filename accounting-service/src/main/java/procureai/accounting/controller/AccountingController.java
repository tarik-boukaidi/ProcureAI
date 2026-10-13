package procureai.accounting.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import procureai.accounting.dto.AccountingEntryRequest;
import procureai.accounting.dto.AccountingEntryResponse;
import procureai.accounting.dto.Balance;
import procureai.accounting.dto.MonthlyTotal;
import procureai.accounting.service.AccountingService;

import java.util.List;

// Pas de PUT ni de DELETE : une ecriture comptable ne se modifie pas.
// Lecture : le comptable et l'admin (qui controle sans pouvoir modifier).
@RestController
@RequestMapping("/api/accounting")
public class AccountingController {

    private final AccountingService service;

    public AccountingController(AccountingService service) {
        this.service = service;
    }

    // Appele par invoice-service (le gateway bloque les POST venant de l'exterieur)
    @PostMapping("/entries")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountingEntryResponse recordEntry(@Valid @RequestBody AccountingEntryRequest request) {
        return service.recordEntry(request);
    }

    @GetMapping("/entries")
    public List<AccountingEntryResponse> getAllEntries(@RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "COMPTABLE", "ADMIN");
        return service.getAllEntries();
    }

    @GetMapping("/entries/invoice/{invoiceId}")
    public List<AccountingEntryResponse> getEntriesByInvoice(@PathVariable Long invoiceId,
                                                             @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "COMPTABLE", "ADMIN");
        return service.getEntriesByInvoice(invoiceId);
    }

    @GetMapping("/suppliers/{supplierId}/balance")
    public Balance getSupplierBalance(@PathVariable Long supplierId,
                                      @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "COMPTABLE", "ADMIN");
        return service.getSupplierBalance(supplierId);
    }

    @GetMapping("/outstanding")
    public Balance getOutstanding(@RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "COMPTABLE", "ADMIN");
        return service.getOutstanding();
    }

    @GetMapping("/report")
    public List<MonthlyTotal> getMonthlyReport(@RequestParam int year,
                                               @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "COMPTABLE", "ADMIN");
        return service.getMonthlyReport(year);
    }

    // Le role vient du JWT : le gateway le met dans le header X-User-Role
    private void checkRole(String userRole, String... allowedRoles) {
        if (userRole == null || !List.of(allowedRoles).contains(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role: " + userRole);
        }
    }
}
