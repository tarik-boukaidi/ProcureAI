package procureai.accounting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import procureai.accounting.entity.AccountingEntry;

import java.math.BigDecimal;
import java.time.LocalDate;

// L'appelant ne choisit pas les comptes debit / credit : ils dependent du type
public record AccountingEntryRequest(
        @NotNull AccountingEntry.Type type,
        @NotNull Long invoiceId,
        @NotNull Long supplierId,
        @NotNull @Positive BigDecimal amount,
        String description,
        LocalDate entryDate) {
}
