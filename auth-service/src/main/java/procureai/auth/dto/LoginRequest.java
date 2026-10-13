package procureai.auth.dto;

import jakarta.validation.constraints.NotBlank;

// Envoye dans le corps de la requete : un mot de passe ne doit jamais passer dans l'URL (logs du gateway, de nginx...)
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password) {
}
