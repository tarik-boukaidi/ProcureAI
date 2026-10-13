package procureai.demande.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import procureai.demande.messaging.NotificationPublisher;
import procureai.demande.messaging.NotificationRequest;
import procureai.demande.dto.CreateDemandeRequest;
import procureai.demande.dto.DemandeResponse;
import procureai.demande.entity.PurchaseRequest;
import procureai.demande.entity.PurchaseRequestLine;
import procureai.demande.repository.PurchaseRequestRepository;

import java.util.List;

@Service
@Transactional
public class PurchaseRequestServiceImpl implements PurchaseRequestService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseRequestServiceImpl.class);

    private final PurchaseRequestRepository repository;
    private final NotificationPublisher notificationPublisher;

    public PurchaseRequestServiceImpl(PurchaseRequestRepository repository, NotificationPublisher notificationPublisher) {
        this.repository = repository;
        this.notificationPublisher = notificationPublisher;
    }

    @Override
    public DemandeResponse createDemande(CreateDemandeRequest body, Long requesterId) {
        if (body.product() == null || body.product().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le produit est obligatoire");
        }
        if (body.quantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La quantité doit être supérieure à 0");
        }

        PurchaseRequest request = new PurchaseRequest();
        request.setReason(body.reason());
        request.setRequesterId(requesterId);

        PurchaseRequestLine line = new PurchaseRequestLine();
        line.setProduct(body.product());
        line.setQuantity(body.quantity());
        line.setPurchaseRequest(request);
        request.getItems().add(line);

        PurchaseRequest saved = repository.save(request);
        notify(new NotificationRequest(null, "MANAGER", "DEMANDE_CREATED",
                "Nouvelle demande #" + saved.getId() + " a approuver"));
        return DemandeResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DemandeResponse getDemandeById(Long id) {
        return DemandeResponse.from(findDemande(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponse> getAllDemandes() {
        return repository.findAll().stream().map(DemandeResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponse> getDemandesByRequester(Long requesterId) {
        return repository.findByRequesterId(requesterId).stream().map(DemandeResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponse> getDemandesByStatus(PurchaseRequest.Status status) {
        return repository.findByStatus(status).stream().map(DemandeResponse::from).toList();
    }

    @Override
    public DemandeResponse approveDemande(Long id) {
        PurchaseRequest request = changeStatus(id, PurchaseRequest.Status.APPROVED);
        notify(new NotificationRequest(request.getRequesterId(), null, "DEMANDE_APPROVED",
                "Votre demande #" + id + " est approuvee"));
        return DemandeResponse.from(request);
    }

    @Override
    public DemandeResponse rejectDemande(Long id) {
        PurchaseRequest request = changeStatus(id, PurchaseRequest.Status.REJECTED);
        notify(new NotificationRequest(request.getRequesterId(), null, "DEMANDE_REJECTED",
                "Votre demande #" + id + " est rejetee"));
        return DemandeResponse.from(request);
    }

    @Override
    public void removeDemande(Long id, Long userId) {
        PurchaseRequest request = findDemande(id);

        if (!request.getRequesterId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le createur peut supprimer cette demande");
        }
        if (request.getStatus() != PurchaseRequest.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seule une demande PENDING peut etre supprimee, statut actuel : " + request.getStatus());
        }
        repository.delete(request);
    }

    private PurchaseRequest findDemande(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable : " + id));
    }

    private PurchaseRequest changeStatus(Long id, PurchaseRequest.Status newStatus) {
        PurchaseRequest request = findDemande(id);
        if (request.getStatus() != PurchaseRequest.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seule une demande PENDING peut être traitée, statut actuel : " + request.getStatus());
        }
        request.setStatus(newStatus);
        return repository.save(request);
    }

    // Une notification ratee ne doit pas annuler la demande : on log l'erreur et on continue
    private void notify(NotificationRequest notification) {
        try {
            notificationPublisher.send(notification);
        } catch (Exception e) {
            log.warn("Notification not sent: {}", e.getMessage());
        }
    }
}
