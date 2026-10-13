package procureai.supplier.service;

import procureai.supplier.dto.SupplierRequest;
import procureai.supplier.dto.SupplierResponse;

import java.util.List;

public interface SupplierService {

    SupplierResponse createSupplier(SupplierRequest request);

    SupplierResponse getSupplier(Long id);

    List<SupplierResponse> getAllSuppliers();

    List<SupplierResponse> getSuppliersByStatus(String status);

    SupplierResponse updateSupplier(Long id, SupplierRequest request);

    void deleteSupplier(Long id);
}
