package procureai.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Creation d'une commande a partir d'une demande approuvee.
// orderNumber est facultatif : genere ("PO-XXXXXXXX") s'il est vide. Le total est calcule par le serveur.
public record PurchaseOrderRequest(
        String orderNumber,
        @NotNull Long purchaseRequestId,
        @NotNull Long supplierId,
        LocalDate expectedDeliveryDate,
        @NotEmpty @Valid List<Line> lines) {

    public record Line(
            @NotNull Long productId,
            String productName,
            @Positive int quantity,
            @NotNull @Positive BigDecimal unitPrice) {
    }
}
