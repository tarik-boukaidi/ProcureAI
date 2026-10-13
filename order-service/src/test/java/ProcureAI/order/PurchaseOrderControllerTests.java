package procureai.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.jayway.jsonpath.JsonPath;
import procureai.order.messaging.NotificationPublisher;
import procureai.order.client.ProcurementClient;
import procureai.order.client.PurchaseRequestResponse;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PurchaseOrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProcurementClient procurementClient;

    @MockBean
    private NotificationPublisher notificationPublisher;

    @Test
    void createOrderComputesTotalAndGeneratesOrderNumber() throws Exception {
        when(procurementClient.getDemandeById(1L))
                .thenReturn(new PurchaseRequestResponse(1L, 5L, "APPROVED", List.of()));

        String body = """
                {
                  "purchaseRequestId": 1,
                  "supplierId": 7,
                  "lines": [
                    {"productId": 10, "productName": "Laptop", "quantity": 2, "unitPrice": 500.00},
                    {"productId": 11, "productName": "Mouse", "quantity": 3, "unitPrice": 10.50}
                  ]
                }
                """;

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber", startsWith("PO-")))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.totalAmount").value(1031.50))
                .andExpect(jsonPath("$.lines", hasSize(2)));

        mockMvc.perform(get("/api/orders/supplier/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void unknownOrderReturns404() throws Exception {
        mockMvc.perform(get("/api/orders/999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/orders/number/NOPE")).andExpect(status().isNotFound());
    }

    @Test
    void invalidStatusReturns400() throws Exception {
        mockMvc.perform(get("/api/orders/status/UNKNOWN")).andExpect(status().isBadRequest());
    }

    @Test
    void adminFollowsTheOrderLifecycle() throws Exception {
        long id = createOrder(2L);

        mockMvc.perform(put("/api/orders/" + id + "/status?status=SENT").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"));
        verify(notificationPublisher).send(argThat(n ->
                "MAGASINIER".equals(n.recipientRole()) && "ORDER_SENT".equals(n.type())));

        mockMvc.perform(put("/api/orders/" + id + "/status?status=CONFIRMED").header("X-User-Role", "ADMIN"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        mockMvc.perform(put("/api/orders/" + id + "/status?status=DELIVERED").header("X-User-Role", "ADMIN"))
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        // Une commande livree ne change plus
        mockMvc.perform(put("/api/orders/" + id + "/status?status=CANCELLED").header("X-User-Role", "ADMIN"))
                .andExpect(status().isConflict());
    }

    @Test
    void draftCannotBeDeliveredDirectly() throws Exception {
        long id = createOrder(3L);
        mockMvc.perform(put("/api/orders/" + id + "/status?status=DELIVERED").header("X-User-Role", "ADMIN"))
                .andExpect(status().isConflict());
    }

    @Test
    void onlyAdminChangesOrderStatus() throws Exception {
        long id = createOrder(4L);
        mockMvc.perform(put("/api/orders/" + id + "/status?status=SENT").header("X-User-Role", "MANAGER"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/orders/" + id + "/status?status=SENT"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/orders/999/status?status=SENT").header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    private long createOrder(Long demandeId) throws Exception {
        when(procurementClient.getDemandeById(demandeId))
                .thenReturn(new PurchaseRequestResponse(demandeId, 5L, "APPROVED", List.of()));
        String body = """
                {"purchaseRequestId": %d, "supplierId": 8,
                 "lines": [{"productId": 10, "productName": "Laptop", "quantity": 1, "unitPrice": 500.00}]}
                """.formatted(demandeId);
        String json = mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.parse(json).read("$.id", Long.class);
    }
}
