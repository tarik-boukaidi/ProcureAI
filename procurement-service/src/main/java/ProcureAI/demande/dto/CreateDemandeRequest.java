package procureai.demande.dto;

// Le demandeur vient du JWT (header X-User-Id), pas du corps
public record CreateDemandeRequest(String product, int quantity, String reason) {
}
