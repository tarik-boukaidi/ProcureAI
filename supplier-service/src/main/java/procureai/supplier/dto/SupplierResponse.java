package procureai.supplier.dto;

import procureai.supplier.entity.Supplier;

public record SupplierResponse(
        Long id,
        String name,
        String cin,
        String ice,
        String email,
        String phone,
        String address,
        String city,
        String status) {

    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(supplier.getId(), supplier.getName(), supplier.getCin(), supplier.getIce(),
                supplier.getEmail(), supplier.getPhone(), supplier.getAddress(), supplier.getCity(), supplier.getStatus());
    }
}
