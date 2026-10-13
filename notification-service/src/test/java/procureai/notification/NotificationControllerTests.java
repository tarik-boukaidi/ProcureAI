package procureai.notification;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import procureai.notification.repository.NotificationRepository;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@AutoConfigureMockMvc
class NotificationControllerTests {

    private static final String FOR_USER_5 = """
            {"recipientUserId": 5, "type": "DEMANDE_APPROVED", "message": "Votre demande #12 est approuvee"}
            """;

    private static final String FOR_MANAGERS = """
            {"recipientRole": "MANAGER", "type": "DEMANDE_CREATED", "message": "Nouvelle demande #12 a approuver"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private Long send(String json) throws Exception {
        String body = mockMvc.perform(post("/api/notifications").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    @Test
    void sendNotificationToUser() throws Exception {
        mockMvc.perform(post("/api/notifications").contentType(MediaType.APPLICATION_JSON).content(FOR_USER_5))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recipientUserId").value(5))
                .andExpect(jsonPath("$.type").value("DEMANDE_APPROVED"))
                .andExpect(jsonPath("$.read").value(false))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void notificationWithoutRecipientReturns400() throws Exception {
        mockMvc.perform(post("/api/notifications").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"INVOICE_PAID\", \"message\": \"Facture payee\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void notificationWithoutMessageReturns400() throws Exception {
        mockMvc.perform(post("/api/notifications").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientUserId\": 5, \"type\": \"INVOICE_PAID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userSeesOwnAndRoleNotifications() throws Exception {
        send(FOR_USER_5);
        send(FOR_MANAGERS);
        send("""
                {"recipientUserId": 9, "type": "DEMANDE_REJECTED", "message": "Pas pour user 5"}
                """);

        // user 5 est MANAGER : il voit sa notification + celle des managers
        mockMvc.perform(get("/api/notifications/mine").header("X-User-Id", 5).header("X-User-Role", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // user 5 est EMPLOYEE : il voit seulement sa notification
        mockMvc.perform(get("/api/notifications/mine").header("X-User-Id", 5).header("X-User-Role", "EMPLOYEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recipientUserId").value(5));
    }

    @Test
    void personalNotificationIsNotVisibleToSameRole() throws Exception {
        Long id = send("""
                {"recipientUserId": 5, "recipientRole": "EMPLOYEE", "type": "DEMANDE_APPROVED", "message": "Pour user 5 seulement"}
                """);

        mockMvc.perform(get("/api/notifications/mine").header("X-User-Id", 9).header("X-User-Role", "EMPLOYEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(put("/api/notifications/" + id + "/read").header("X-User-Id", 9).header("X-User-Role", "EMPLOYEE"))
                .andExpect(status().isForbidden());
    }

    @Test
    void recipientCanMarkAsRead() throws Exception {
        Long id = send(FOR_USER_5);

        mockMvc.perform(put("/api/notifications/" + id + "/read").header("X-User-Id", 5).header("X-User-Role", "EMPLOYEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void managerCanMarkRoleNotificationAsRead() throws Exception {
        Long id = send(FOR_MANAGERS);

        mockMvc.perform(put("/api/notifications/" + id + "/read").header("X-User-Id", 3).header("X-User-Role", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void otherUserCannotMarkAsRead() throws Exception {
        Long id = send(FOR_USER_5);

        mockMvc.perform(put("/api/notifications/" + id + "/read").header("X-User-Id", 9).header("X-User-Role", "EMPLOYEE"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownNotificationReturns404() throws Exception {
        mockMvc.perform(put("/api/notifications/999/read").header("X-User-Id", 5).header("X-User-Role", "EMPLOYEE"))
                .andExpect(status().isNotFound());
    }
}
