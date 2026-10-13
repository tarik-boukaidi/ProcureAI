package procureai.reception.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.util.List;

// Ce que le magasinier saisit. La quantite commandee n'est pas envoyee : elle est relue dans order-service.
public record ReceptionRequest(
        @NotNull Long purchaseOrderId,
        @NotNull Long supplierId,
        String deliveryNoteNumber,
        LocalDate receivedDate,
        String comment,
        @NotEmpty @Valid List<Line> lines) {

    public record Line(
            @NotNull Long productId,
            String productName,
            @PositiveOrZero int quantityReceived,
            @PositiveOrZero int quantityRejected) {
    }
}
