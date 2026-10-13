package procureai.invoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import procureai.invoice.entity.Invoice;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByPurchaseOrderId(Long purchaseOrderId);

    List<Invoice> findBySupplierId(Long supplierId);

    List<Invoice> findByStatus(Invoice.Status status);

    boolean existsBySupplierIdAndInvoiceNumber(Long supplierId, String invoiceNumber);
}
