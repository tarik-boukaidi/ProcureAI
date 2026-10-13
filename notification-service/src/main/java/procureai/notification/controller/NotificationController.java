package procureai.notification.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import procureai.notification.dto.NotificationRequest;
import procureai.notification.dto.NotificationResponse;
import procureai.notification.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Appele par les autres services (procurement, invoice, payment...)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse sendNotification(@Valid @RequestBody NotificationRequest request) {
        return notificationService.sendNotification(request);
    }

    // L'utilisateur connecte : le gateway met son id et son role dans les headers
    @GetMapping("/mine")
    public List<NotificationResponse> getMyNotifications(@RequestHeader("X-User-Id") Long userId,
                                                         @RequestHeader(value = "X-User-Role", required = false) String role) {
        return notificationService.getMyNotifications(userId, role);
    }

    @PutMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable Long id,
                                           @RequestHeader("X-User-Id") Long userId,
                                           @RequestHeader(value = "X-User-Role", required = false) String role) {
        return notificationService.markAsRead(id, userId, role);
    }
}
