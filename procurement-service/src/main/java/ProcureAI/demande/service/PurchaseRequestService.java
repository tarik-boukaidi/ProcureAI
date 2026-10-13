package procureai.demande.service;

import procureai.demande.dto.CreateDemandeRequest;
import procureai.demande.dto.DemandeResponse;
import procureai.demande.entity.PurchaseRequest;

import java.util.List;

public interface PurchaseRequestService {

    DemandeResponse createDemande(CreateDemandeRequest request, Long requesterId);

    DemandeResponse getDemandeById(Long id);

    List<DemandeResponse> getAllDemandes();

    List<DemandeResponse> getDemandesByRequester(Long requesterId);

    List<DemandeResponse> getDemandesByStatus(PurchaseRequest.Status status);

    DemandeResponse approveDemande(Long id);

    DemandeResponse rejectDemande(Long id);

    void removeDemande(Long id, Long userId);
}
