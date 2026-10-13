package procureai.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import procureai.supplier.entity.Product.Category;

import java.math.BigDecimal;

// Le fournisseur vient de l'URL (/api/suppliers/{id}/products), pas du corps
public record ProductRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal price,
        Category category) {
}
