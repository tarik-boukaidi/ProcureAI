package procureai.notification.service;

import procureai.notification.dto.NotificationRequest;
import procureai.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse sendNotification(NotificationRequest request);

    List<NotificationResponse> getMyNotifications(Long userId, String role);

    NotificationResponse markAsRead(Long id, Long userId, String role);
}
