package procureai.supplier.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import procureai.supplier.dto.SupplierRequest;
import procureai.supplier.dto.SupplierResponse;
import procureai.supplier.entity.Supplier;
import procureai.supplier.repository.SupplierRepository;

import java.util.List;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repository;

    public SupplierServiceImpl(SupplierRepository repository) {
        this.repository = repository;
    }

    @Override
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (repository.existsByCin(request.cin())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Supplier already exists");
        }
        Supplier supplier = new Supplier();
        copy(request, supplier);
        return SupplierResponse.from(repository.save(supplier));
    }

    @Override
    public SupplierResponse getSupplier(Long id) {
        return SupplierResponse.from(findSupplier(id));
    }

    @Override
    public List<SupplierResponse> getAllSuppliers() {
        return repository.findAll().stream().map(SupplierResponse::from).toList();
    }

    @Override
    public List<SupplierResponse> getSuppliersByStatus(String status) {
        return repository.findByStatus(status).stream().map(SupplierResponse::from).toList();
    }

    @Override
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier existing = findSupplier(id);
        if (!existing.getCin().equals(request.cin()) && repository.existsByCin(request.cin())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another supplier already uses this CIN");
        }
        copy(request, existing);
        return SupplierResponse.from(repository.save(existing));
    }

    @Override
    public void deleteSupplier(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found");
        }
        repository.deleteById(id);
    }

    private Supplier findSupplier(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
    }

    private void copy(SupplierRequest request, Supplier supplier) {
        supplier.setName(request.name());
        supplier.setCin(request.cin());
        // ICE vide = pas d'ICE
        supplier.setIce(request.ice() == null || request.ice().isBlank() ? null : request.ice());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
        supplier.setAddress(request.address());
        supplier.setCity(request.city());
        supplier.setStatus(request.status());
    }
}
