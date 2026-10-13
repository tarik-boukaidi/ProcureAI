package procureai.supplier.service;

import procureai.supplier.dto.ProductRequest;
import procureai.supplier.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> getProducts(Long supplierId);

    ProductResponse getProduct(Long supplierId, Long productId);

    ProductResponse createProduct(Long supplierId, ProductRequest request);

    ProductResponse updateProduct(Long supplierId, Long productId, ProductRequest request);

    void deleteProduct(Long supplierId, Long productId);
}
