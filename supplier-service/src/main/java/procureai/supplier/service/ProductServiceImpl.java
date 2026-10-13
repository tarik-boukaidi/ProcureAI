package procureai.supplier.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import procureai.supplier.dto.ProductRequest;
import procureai.supplier.dto.ProductResponse;
import procureai.supplier.entity.Product;
import procureai.supplier.entity.Supplier;
import procureai.supplier.repository.ProductRepository;
import procureai.supplier.repository.SupplierRepository;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final SupplierRepository supplierRepository;

    public ProductServiceImpl(ProductRepository repository, SupplierRepository supplierRepository) {
        this.repository = repository;
        this.supplierRepository = supplierRepository;
    }

    @Override
    public List<ProductResponse> getProducts(Long supplierId) {
        findSupplier(supplierId); // 404 si le fournisseur n'existe pas
        return repository.findBySupplier_Id(supplierId).stream().map(ProductResponse::from).toList();
    }

    @Override
    public ProductResponse getProduct(Long supplierId, Long productId) {
        return ProductResponse.from(findProduct(supplierId, productId));
    }

    @Override
    public ProductResponse createProduct(Long supplierId, ProductRequest request) {
        Product product = new Product();
        product.setSupplier(findSupplier(supplierId));
        copy(request, product);
        return ProductResponse.from(repository.save(product));
    }

    @Override
    public ProductResponse updateProduct(Long supplierId, Long productId, ProductRequest request) {
        Product existing = findProduct(supplierId, productId);
        copy(request, existing);
        return ProductResponse.from(repository.save(existing));
    }

    @Override
    public void deleteProduct(Long supplierId, Long productId) {
        repository.delete(findProduct(supplierId, productId));
    }

    private Supplier findSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
    }

    private Product findProduct(Long supplierId, Long productId) {
        return repository.findByIdAndSupplier_Id(productId, supplierId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void copy(ProductRequest request, Product product) {
        product.setName(request.name());
        product.setPrice(request.price());
        product.setCategory(request.category());
    }
}
