package procureai.notification.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import procureai.notification.dto.NotificationRequest;
import procureai.notification.service.NotificationService;

@Component
public class NotificationEventListener {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public NotificationEventListener(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void receive(String message) throws JsonProcessingException {
        NotificationRequest notification = objectMapper.readValue(message, NotificationRequest.class);
        notificationService.sendNotification(notification);
    }
}
