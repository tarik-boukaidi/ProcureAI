package procureai.demande.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import procureai.demande.dto.CreateDemandeRequest;
import procureai.demande.dto.DemandeResponse;
import procureai.demande.entity.PurchaseRequest;
import procureai.demande.service.PurchaseRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/demandes")
public class DemandeController {

    private final PurchaseRequestService service;

    public DemandeController(PurchaseRequestService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeResponse create(@RequestBody CreateDemandeRequest body,
                                  @RequestHeader("X-User-Id") Long requesterId) {
        return service.createDemande(body, requesterId);
    }

    @GetMapping("/{id}")
    public DemandeResponse getDemande(@PathVariable Long id) {
        return service.getDemandeById(id);
    }

    @GetMapping
    public List<DemandeResponse> getAllDemandes() {
        return service.getAllDemandes();
    }

    @GetMapping("/mine")
    public List<DemandeResponse> getMyDemandes(@RequestHeader("X-User-Id") Long requesterId) {
        return service.getDemandesByRequester(requesterId);
    }

    @GetMapping("/status/{status}")
    public List<DemandeResponse> getDemandesByStatus(@PathVariable PurchaseRequest.Status status) {
        return service.getDemandesByStatus(status);
    }

    @PutMapping("/{id}/approve")
    public DemandeResponse approve(@PathVariable Long id,
                                   @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "MANAGER", "ADMIN");
        return service.approveDemande(id);
    }

    @PutMapping("/{id}/reject")
    public DemandeResponse reject(@PathVariable Long id,
                                  @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "MANAGER", "ADMIN");
        return service.rejectDemande(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        service.removeDemande(id, userId);
    }

    // Le role vient du JWT : le gateway le met dans le header X-User-Role
    private void checkRole(String userRole, String... allowedRoles) {
        if (userRole == null || !List.of(allowedRoles).contains(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role: " + userRole);
        }
    }
}
