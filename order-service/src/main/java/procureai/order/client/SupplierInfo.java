package procureai.order.client;

public record SupplierInfo(
        Long id,
        String name,
        String cin,
        String ice,
        String email,
        String phone,
        String address,
        String city,
        String status) {
}
