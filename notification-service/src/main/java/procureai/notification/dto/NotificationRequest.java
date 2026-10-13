package procureai.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import procureai.notification.entity.Notification;

// Destinataire : soit un utilisateur precis (recipientUserId), soit tous les utilisateurs d'un role
public record NotificationRequest(
        Long recipientUserId,
        String recipientRole,
        @NotNull Notification.Type type,
        @NotBlank String message) {
}
