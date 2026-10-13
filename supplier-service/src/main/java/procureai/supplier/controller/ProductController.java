package procureai.supplier.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import procureai.supplier.dto.ProductRequest;
import procureai.supplier.dto.ProductResponse;
import procureai.supplier.service.ProductService;

import java.util.List;

// Le catalogue d'un fournisseur : tout le monde peut le lire, seul l'ADMIN le modifie
@RestController
@RequestMapping("/api/suppliers/{supplierId}/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductResponse> getProducts(@PathVariable Long supplierId) {
        return service.getProducts(supplierId);
    }

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable Long supplierId, @PathVariable Long productId) {
        return service.getProduct(supplierId, productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@PathVariable Long supplierId,
                                         @Valid @RequestBody ProductRequest request,
                                         @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "ADMIN");
        return service.createProduct(supplierId, request);
    }

    @PutMapping("/{productId}")
    public ProductResponse updateProduct(@PathVariable Long supplierId,
                                         @PathVariable Long productId,
                                         @Valid @RequestBody ProductRequest request,
                                         @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "ADMIN");
        return service.updateProduct(supplierId, productId, request);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long supplierId,
                              @PathVariable Long productId,
                              @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkRole(role, "ADMIN");
        service.deleteProduct(supplierId, productId);
    }

    // Le role vient du JWT : le gateway le met dans le header X-User-Role
    private void checkRole(String userRole, String... allowedRoles) {
        if (userRole == null || !List.of(allowedRoles).contains(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role: " + userRole);
        }
    }
}
