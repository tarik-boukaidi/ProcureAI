package procureai.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Creation / modification d'un fournisseur
public record SupplierRequest(
        @NotBlank String name,
        @NotBlank String cin,
        @Pattern(regexp = "(\\d{15})?", message = "ICE must have 15 digits") String ice,
        @Email String email,
        String phone,
        String address,
        String city,
        String status) {
}
