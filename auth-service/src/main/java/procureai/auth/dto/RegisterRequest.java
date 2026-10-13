package procureai.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Inscription publique : le compte cree est toujours un EMPLOYEE (le role n'est pas choisi par l'utilisateur)
public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        String phone,
        String address) {
}
