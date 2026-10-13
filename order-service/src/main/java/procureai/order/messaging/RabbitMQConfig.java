package procureai.order.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "procureai.events";
    public static final String NOTIFICATION_QUEUE = "notifications.queue";

    @Bean
    TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    Queue notificationsQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    Binding notificationBinding(Queue notificationsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(eventsExchange).with("notification.#");
    }
}
