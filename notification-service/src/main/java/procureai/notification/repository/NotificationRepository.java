package procureai.notification.repository;

import procureai.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Notifications personnelles de l'utilisateur + notifications de groupe de son role
    @Query("""
            SELECT n FROM Notification n
            WHERE n.recipientUserId = :userId
               OR (n.recipientUserId IS NULL AND n.recipientRole = :role)
            ORDER BY n.createdAt DESC
            """)
    List<Notification> findMyNotifications(Long userId, String role);
}
