package procureai.auth.dto;

// Le JWT contient deja l'id, l'email et le role de l'utilisateur
public record LoginResponse(String token) {
}
