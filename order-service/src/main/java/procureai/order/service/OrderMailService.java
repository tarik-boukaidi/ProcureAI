package procureai.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import procureai.order.entity.PurchaseOrder;
import procureai.order.entity.PurchaseOrderLine;

@Service
public class OrderMailService {

    private static final Logger log = LoggerFactory.getLogger(OrderMailService.class);

    private final JavaMailSender mailSender;
    private final String host;
    private final String from;
    private final String frontendUrl;

    public OrderMailService(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${spring.mail.host:}") String host,
            @Value("${app.mail.from:noreply@procureai.local}") String from,
            @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.mailSender = mailSender;
        this.host = host;
        this.from = from;
        this.frontendUrl = frontendUrl;
    }

    public void sendOrder(String to, String supplierName, PurchaseOrder order) {
        if (to == null || to.isBlank()) {
            log.warn("Fournisseur sans e-mail, commande {} non envoyee", order.getOrderNumber());
            return;
        }
        String body = body(supplierName, order);
        if (host == null || host.isBlank() || mailSender == null) {
            log.info("SMTP non configure. E-mail pour {} :\n{}", to, body);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Commande " + order.getOrderNumber());
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("E-mail non envoye a {} : {}", to, e.getMessage());
        }
    }

    private String body(String supplierName, PurchaseOrder order) {
        StringBuilder text = new StringBuilder();
        text.append("Bonjour");
        if (supplierName != null && !supplierName.isBlank()) {
            text.append(" ").append(supplierName);
        }
        text.append(",\n\nVous avez une nouvelle commande ").append(order.getOrderNumber()).append(".\n\n");
        for (PurchaseOrderLine line : order.getLines()) {
            text.append("- ").append(line.getProductName())
                    .append(" × ").append(line.getQuantity())
                    .append(" (").append(line.getUnitPrice()).append(" MAD)\n");
        }
        if (order.getExpectedDeliveryDate() != null) {
            text.append("\nLivraison souhaitée : ").append(order.getExpectedDeliveryDate()).append("\n");
        }
        text.append("\nConnectez-vous pour la confirmer : ").append(frontendUrl).append("/login\n");
        return text.toString();
    }
}
