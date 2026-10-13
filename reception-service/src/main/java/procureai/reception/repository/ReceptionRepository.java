package procureai.reception.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import procureai.reception.entity.Reception;

import java.util.List;

@Repository
public interface ReceptionRepository extends JpaRepository<Reception, Long> {

    List<Reception> findByPurchaseOrderId(Long purchaseOrderId);
}
