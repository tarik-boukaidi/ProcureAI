package procureai.order.messaging;

public record NotificationRequest(Long recipientUserId, String recipientRole, String type, String message) {
}
