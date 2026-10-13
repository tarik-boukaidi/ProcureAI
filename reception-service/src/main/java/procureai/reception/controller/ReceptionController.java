package procureai.reception.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import procureai.reception.dto.ReceptionRequest;
import procureai.reception.dto.ReceptionResponse;
import procureai.reception.service.ReceptionService;

import java.util.List;

@RestController
@RequestMapping("/api/receptions")
public class ReceptionController {

    private final ReceptionService receptionService;

    public ReceptionController(ReceptionService receptionService) {
        this.receptionService = receptionService;
    }

    // Le magasinier controle la livraison (quantites, produits casses) et l'enregistre
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReceptionResponse createReception(@Valid @RequestBody ReceptionRequest request,
                                             @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "MAGASINIER", "ADMIN");
        return receptionService.createReception(request);
    }

    @GetMapping("/{id}")
    public ReceptionResponse getReception(@PathVariable Long id) {
        return receptionService.getReceptionById(id);
    }

    @GetMapping
    public List<ReceptionResponse> getReceptions() {
        return receptionService.getAllReceptions();
    }

    @GetMapping("/order/{purchaseOrderId}")
    public List<ReceptionResponse> getReceptionsByOrder(@PathVariable Long purchaseOrderId) {
        return receptionService.getReceptionsByOrder(purchaseOrderId);
    }

    // Le role vient du JWT : le gateway le met dans le header X-User-Role
    private void checkRole(String userRole, String... allowedRoles) {
        if (userRole == null || !List.of(allowedRoles).contains(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role: " + userRole);
        }
    }
}
