package procureai.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Ce que le comptable saisit. Les totaux HT / TVA / TTC et le statut sont calcules par le service.
public record InvoiceRequest(
        @NotBlank String invoiceNumber,
        @NotNull Long purchaseOrderId,
        @NotNull Long supplierId,
        LocalDate invoiceDate,
        LocalDate dueDate,
        BigDecimal vatRate,
        @NotEmpty @Valid List<Line> lines) {

    public record Line(
            @NotNull Long productId,
            String productName,
            @Positive int quantity,
            @NotNull @Positive BigDecimal unitPrice) {
    }
}
