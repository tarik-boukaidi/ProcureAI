package procureai.accounting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import procureai.accounting.entity.AccountingEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountingEntryRepository extends JpaRepository<AccountingEntry, Long> {

    Optional<AccountingEntry> findByTypeAndInvoiceId(AccountingEntry.Type type, Long invoiceId);

    List<AccountingEntry> findByInvoiceId(Long invoiceId);

    List<AccountingEntry> findByTypeAndEntryDateBetween(AccountingEntry.Type type, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM AccountingEntry e WHERE e.type = :type")
    BigDecimal sumByType(AccountingEntry.Type type);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM AccountingEntry e WHERE e.type = :type AND e.supplierId = :supplierId")
    BigDecimal sumByTypeAndSupplier(AccountingEntry.Type type, Long supplierId);
}
