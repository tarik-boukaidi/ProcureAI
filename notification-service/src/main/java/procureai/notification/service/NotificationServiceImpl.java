package procureai.notification.service;

import procureai.notification.dto.NotificationRequest;
import procureai.notification.dto.NotificationResponse;
import procureai.notification.entity.Notification;
import procureai.notification.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository repository;

    public NotificationServiceImpl(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public NotificationResponse sendNotification(NotificationRequest request) {
        if (request.recipientUserId() == null && request.recipientRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "recipientUserId or recipientRole is required");
        }

        // Toujours une nouvelle notification non lue (le constructeur met read = false et la date)
        Notification notification = new Notification();
        notification.setRecipientUserId(request.recipientUserId());
        notification.setRecipientRole(request.recipientRole());
        notification.setType(request.type());
        notification.setMessage(request.message());
        return NotificationResponse.from(repository.save(notification));
    }

    @Override
    public List<NotificationResponse> getMyNotifications(Long userId, String role) {
        return repository.findMyNotifications(userId, role).stream().map(NotificationResponse::from).toList();
    }

    @Override
    public NotificationResponse markAsRead(Long id, Long userId, String role) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!isRecipient(notification, userId, role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This notification is not for you");
        }

        notification.setRead(true);
        return NotificationResponse.from(repository.save(notification));
    }

    // Notification personnelle (recipientUserId rempli) : on verifie seulement l'id, le role est ignore
    private boolean isRecipient(Notification notification, Long userId, String role) {
        if (notification.getRecipientUserId() != null) {
            return notification.getRecipientUserId().equals(userId);
        }
        return notification.getRecipientRole() != null && notification.getRecipientRole().equals(role);
    }
}
