package procureai.demande;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import procureai.demande.messaging.NotificationPublisher;
import procureai.demande.messaging.NotificationRequest;
import procureai.demande.repository.PurchaseRequestRepository;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemandeControllerTests {

    private static final String LAPTOP_DEMANDE = """
            {"product": "Laptop", "quantity": 3, "reason": "Nouveaux employes"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PurchaseRequestRepository repository;

    @MockBean
    private NotificationPublisher notificationPublisher;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private Integer createDemande(long requesterId) throws Exception {
        String response = mockMvc.perform(post("/api/demandes")
                        .header("X-User-Id", requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LAPTOP_DEMANDE))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    @Test
    void createDemandeIsPendingAndLinkedToRequester() throws Exception {
        mockMvc.perform(post("/api/demandes")
                        .header("X-User-Id", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LAPTOP_DEMANDE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.requesterId").value(5))
                .andExpect(jsonPath("$.reason").value("Nouveaux employes"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].product").value("Laptop"))
                .andExpect(jsonPath("$.items[0].quantity").value(3));

        verify(notificationPublisher).send(any(NotificationRequest.class));
    }

    @Test
    void demandeIsCreatedEvenIfNotificationFails() throws Exception {
        doThrow(new RuntimeException("notification-service down")).when(notificationPublisher).send(any());

        mockMvc.perform(post("/api/demandes")
                        .header("X-User-Id", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LAPTOP_DEMANDE))
                .andExpect(status().isCreated());
    }

    @Test
    void createDemandeWithoutUserHeaderReturns400() throws Exception {
        mockMvc.perform(post("/api/demandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LAPTOP_DEMANDE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emptyProductReturns400() throws Exception {
        mockMvc.perform(post("/api/demandes")
                        .header("X-User-Id", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"product\": \"\", \"quantity\": 3}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void zeroQuantityReturns400() throws Exception {
        mockMvc.perform(post("/api/demandes")
                        .header("X-User-Id", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"product\": \"Laptop\", \"quantity\": 0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mineReturnsOnlyMyDemandes() throws Exception {
        createDemande(5);
        createDemande(5);
        createDemande(8);

        mockMvc.perform(get("/api/demandes/mine").header("X-User-Id", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void approveDemande() throws Exception {
        Integer id = createDemande(5);

        mockMvc.perform(put("/api/demandes/{id}/approve", id).header("X-User-Role", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/api/demandes/status/APPROVED"))
                .andExpect(jsonPath("$", hasSize(1)));

        verify(notificationPublisher).send(new NotificationRequest(5L, null, "DEMANDE_APPROVED",
                "Votre demande #" + id + " est approuvee"));
    }

    @Test
    void rejectDemande() throws Exception {
        Integer id = createDemande(5);

        mockMvc.perform(put("/api/demandes/{id}/reject", id).header("X-User-Role", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(notificationPublisher).send(new NotificationRequest(5L, null, "DEMANDE_REJECTED",
                "Votre demande #" + id + " est rejetee"));
    }

    @Test
    void demandeCanOnlyBeProcessedOnce() throws Exception {
        Integer id = createDemande(5);
        mockMvc.perform(put("/api/demandes/{id}/approve", id).header("X-User-Role", "MANAGER")).andExpect(status().isOk());

        mockMvc.perform(put("/api/demandes/{id}/reject", id).header("X-User-Role", "MANAGER")).andExpect(status().isConflict());
        mockMvc.perform(put("/api/demandes/{id}/approve", id).header("X-User-Role", "MANAGER")).andExpect(status().isConflict());
    }

    @Test
    void employeeCannotApproveOrReject() throws Exception {
        Integer id = createDemande(5);

        mockMvc.perform(put("/api/demandes/{id}/approve", id).header("X-User-Role", "EMPLOYEE"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/demandes/{id}/reject", id).header("X-User-Role", "EMPLOYEE"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/demandes/{id}", id))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void approveWithoutRoleIsForbidden() throws Exception {
        Integer id = createDemande(5);

        mockMvc.perform(put("/api/demandes/{id}/approve", id)).andExpect(status().isForbidden());
    }

    @Test
    void deleteDemande() throws Exception {
        Integer id = createDemande(5);

        mockMvc.perform(delete("/api/demandes/{id}", id).header("X-User-Id", 5)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/demandes/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotDeleteDemande() throws Exception {
        Integer id = createDemande(5);

        mockMvc.perform(delete("/api/demandes/{id}", id).header("X-User-Id", 8)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/demandes/{id}", id)).andExpect(status().isOk());
    }

    @Test
    void approvedDemandeCannotBeDeleted() throws Exception {
        Integer id = createDemande(5);
        mockMvc.perform(put("/api/demandes/{id}/approve", id).header("X-User-Role", "MANAGER")).andExpect(status().isOk());

        mockMvc.perform(delete("/api/demandes/{id}", id).header("X-User-Id", 5)).andExpect(status().isConflict());
    }

    @Test
    void unknownDemandeReturns404() throws Exception {
        mockMvc.perform(get("/api/demandes/999")).andExpect(status().isNotFound());
        mockMvc.perform(put("/api/demandes/999/approve").header("X-User-Role", "ADMIN")).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/demandes/999").header("X-User-Id", 5)).andExpect(status().isNotFound());
    }

    @Test
    void invalidStatusReturns400() throws Exception {
        mockMvc.perform(get("/api/demandes/status/UNKNOWN")).andExpect(status().isBadRequest());
    }
}
