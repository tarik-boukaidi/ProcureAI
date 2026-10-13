package procureai.accounting.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import procureai.accounting.dto.AccountingEntryRequest;
import procureai.accounting.dto.AccountingEntryResponse;
import procureai.accounting.dto.Balance;
import procureai.accounting.dto.MonthlyTotal;
import procureai.accounting.entity.AccountingEntry;
import procureai.accounting.repository.AccountingEntryRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static procureai.accounting.entity.AccountingEntry.*;

@Service
@Transactional
public class AccountingServiceImpl implements AccountingService {

    private final AccountingEntryRepository repository;

    public AccountingServiceImpl(AccountingEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountingEntryResponse recordEntry(AccountingEntryRequest request) {
        // Deja enregistree (ex : l'appelant a reessaye) : on renvoie l'ecriture existante, pas de doublon
        Optional<AccountingEntry> existing = repository.findByTypeAndInvoiceId(request.type(), request.invoiceId());
        if (existing.isPresent()) {
            return AccountingEntryResponse.from(existing.get());
        }

        AccountingEntry entry = new AccountingEntry();
        entry.setType(request.type());
        entry.setInvoiceId(request.invoiceId());
        entry.setSupplierId(request.supplierId());
        entry.setAmount(request.amount());
        entry.setDescription(request.description());
        entry.setEntryDate(request.entryDate() != null ? request.entryDate() : LocalDate.now());

        // Les comptes sont choisis ici, pas par l'appelant : invoice-service ne connait pas la comptabilite
        switch (entry.getType()) {
            case INVOICE_RECORDED -> {
                entry.setDebitAccount(PURCHASES_ACCOUNT);
                entry.setCreditAccount(SUPPLIERS_ACCOUNT);
            }
            case VAT_RECORDED -> {
                entry.setDebitAccount(VAT_ACCOUNT);
                entry.setCreditAccount(SUPPLIERS_ACCOUNT);
            }
            case PAYMENT_RECORDED -> {
                entry.setDebitAccount(SUPPLIERS_ACCOUNT);
                entry.setCreditAccount(BANK_ACCOUNT);
            }
        }
        return AccountingEntryResponse.from(repository.save(entry));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountingEntryResponse> getAllEntries() {
        return repository.findAll().stream().map(AccountingEntryResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountingEntryResponse> getEntriesByInvoice(Long invoiceId) {
        return repository.findByInvoiceId(invoiceId).stream().map(AccountingEntryResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Balance getSupplierBalance(Long supplierId) {
        // Ce qu'on doit au fournisseur = TTC = HT + TVA
        BigDecimal invoiced = repository.sumByTypeAndSupplier(Type.INVOICE_RECORDED, supplierId)
                .add(repository.sumByTypeAndSupplier(Type.VAT_RECORDED, supplierId));
        BigDecimal paid = repository.sumByTypeAndSupplier(Type.PAYMENT_RECORDED, supplierId);
        return new Balance(invoiced, paid, invoiced.subtract(paid));
    }

    @Override
    @Transactional(readOnly = true)
    public Balance getOutstanding() {
        BigDecimal invoiced = repository.sumByType(Type.INVOICE_RECORDED).add(repository.sumByType(Type.VAT_RECORDED));
        BigDecimal paid = repository.sumByType(Type.PAYMENT_RECORDED);
        return new Balance(invoiced, paid, invoiced.subtract(paid));
    }

    // Depenses HT (la TVA est recuperee, ce n'est pas une charge), groupees par mois (les 12 mois, meme vides)
    @Override
    @Transactional(readOnly = true)
    public List<MonthlyTotal> getMonthlyReport(int year) {
        BigDecimal[] totals = new BigDecimal[12];
        for (int i = 0; i < 12; i++) {
            totals[i] = BigDecimal.ZERO;
        }

        List<AccountingEntry> invoices = repository.findByTypeAndEntryDateBetween(
                Type.INVOICE_RECORDED, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        for (AccountingEntry entry : invoices) {
            int monthIndex = entry.getEntryDate().getMonthValue() - 1;
            totals[monthIndex] = totals[monthIndex].add(entry.getAmount());
        }

        List<MonthlyTotal> report = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            report.add(new MonthlyTotal(i + 1, totals[i]));
        }
        return report;
    }
}
