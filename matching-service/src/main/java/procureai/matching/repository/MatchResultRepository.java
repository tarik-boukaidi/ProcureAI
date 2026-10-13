package procureai.matching.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import procureai.matching.entity.MatchResult;

import java.util.List;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    List<MatchResult> findByInvoiceIdOrderByCheckedAtDesc(Long invoiceId);

    List<MatchResult> findByStatus(MatchResult.Status status);
}
