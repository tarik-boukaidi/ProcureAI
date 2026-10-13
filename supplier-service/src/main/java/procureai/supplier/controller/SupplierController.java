package procureai.supplier.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import procureai.supplier.dto.SupplierRequest;
import procureai.supplier.dto.SupplierResponse;
import procureai.supplier.service.SupplierService;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService service;

    public SupplierController(SupplierService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse createSupplier(@Valid @RequestBody SupplierRequest request) {
        return service.createSupplier(request);
    }

    @GetMapping("/{id}")
    public SupplierResponse getSupplier(@PathVariable Long id) {
        return service.getSupplier(id);
    }

    @GetMapping
    public List<SupplierResponse> getAllSuppliers() {
        return service.getAllSuppliers();
    }

    @GetMapping("/status/{status}")
    public List<SupplierResponse> getSuppliersByStatus(@PathVariable String status) {
        return service.getSuppliersByStatus(status);
    }

    @PutMapping("/{id}")
    public SupplierResponse updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return service.updateSupplier(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSupplier(@PathVariable Long id) {
        service.deleteSupplier(id);
    }
}
