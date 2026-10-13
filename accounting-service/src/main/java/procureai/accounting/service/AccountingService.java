package procureai.accounting.service;

import procureai.accounting.dto.AccountingEntryRequest;
import procureai.accounting.dto.AccountingEntryResponse;
import procureai.accounting.dto.Balance;
import procureai.accounting.dto.MonthlyTotal;

import java.util.List;

public interface AccountingService {

    AccountingEntryResponse recordEntry(AccountingEntryRequest request);

    List<AccountingEntryResponse> getAllEntries();

    List<AccountingEntryResponse> getEntriesByInvoice(Long invoiceId);

    Balance getSupplierBalance(Long supplierId);

    Balance getOutstanding();

    List<MonthlyTotal> getMonthlyReport(int year);
}
