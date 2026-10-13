package procureai.accounting.dto;

import procureai.accounting.entity.AccountingEntry;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountingEntryResponse(
        Long id,
        AccountingEntry.Type type,
        Long invoiceId,
        Long supplierId,
        BigDecimal amount,
        String debitAccount,
        String creditAccount,
        String description,
        LocalDate entryDate) {

    public static AccountingEntryResponse from(AccountingEntry entry) {
        return new AccountingEntryResponse(entry.getId(), entry.getType(), entry.getInvoiceId(),
                entry.getSupplierId(), entry.getAmount(), entry.getDebitAccount(), entry.getCreditAccount(),
                entry.getDescription(), entry.getEntryDate());
    }
}
