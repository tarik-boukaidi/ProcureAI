package procureai.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import procureai.payment.entity.Payment;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByInvoiceId(Long invoiceId);

    List<Payment> findBySupplierId(Long supplierId);

    boolean existsByInvoiceId(Long invoiceId);
}
