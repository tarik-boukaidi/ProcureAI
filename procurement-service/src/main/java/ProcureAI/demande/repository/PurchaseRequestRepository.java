package procureai.demande.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import procureai.demande.entity.PurchaseRequest;

import java.util.List;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    List<PurchaseRequest> findByRequesterId(Long requesterId);

    List<PurchaseRequest> findByStatus(PurchaseRequest.Status status);
}
