package procureai.notification.dto;

import procureai.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long recipientUserId,
        String recipientRole,
        Notification.Type type,
        String message,
        boolean read,
        LocalDateTime createdAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getRecipientUserId(),
                notification.getRecipientRole(), notification.getType(), notification.getMessage(),
                notification.isRead(), notification.getCreatedAt());
    }
}
