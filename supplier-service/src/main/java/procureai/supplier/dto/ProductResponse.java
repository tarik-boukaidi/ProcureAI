package procureai.supplier.dto;

import procureai.supplier.entity.Product;
import procureai.supplier.entity.Product.Category;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, BigDecimal price, Category category, Long supplierId) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(),
                product.getCategory(), product.getSupplier().getId());
    }
}
