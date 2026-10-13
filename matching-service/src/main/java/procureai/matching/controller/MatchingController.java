package procureai.matching.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import procureai.matching.dto.MatchResultResponse;
import procureai.matching.entity.MatchResult;
import procureai.matching.service.MatchingService;

import java.util.List;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchingService service;

    public MatchingController(MatchingService service) {
        this.service = service;
    }

    // Le comptable lance le rapprochement : il valide ou rejette la facture
    @PostMapping("/invoices/{invoiceId}")
    @ResponseStatus(HttpStatus.CREATED)
    public MatchResultResponse matchInvoice(@PathVariable Long invoiceId,
                                            @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "COMPTABLE");
        return service.matchInvoice(invoiceId);
    }

    @GetMapping("/{id}")
    public MatchResultResponse getMatchResult(@PathVariable Long id) {
        return service.getMatchResult(id);
    }

    @GetMapping
    public List<MatchResultResponse> getAllMatchResults() {
        return service.getAllMatchResults();
    }

    @GetMapping("/invoice/{invoiceId}")
    public List<MatchResultResponse> getMatchResultsByInvoice(@PathVariable Long invoiceId) {
        return service.getMatchResultsByInvoice(invoiceId);
    }

    @GetMapping("/status/{status}")
    public List<MatchResultResponse> getMatchResultsByStatus(@PathVariable MatchResult.Status status) {
        return service.getMatchResultsByStatus(status);
    }

    // Le role vient du JWT : le gateway le met dans le header X-User-Role
    private void checkRole(String userRole, String... allowedRoles) {
        if (userRole == null || !List.of(allowedRoles).contains(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role: " + userRole);
        }
    }
}
