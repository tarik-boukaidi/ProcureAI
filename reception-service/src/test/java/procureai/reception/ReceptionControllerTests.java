package procureai.reception;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import procureai.reception.client.OrderClient;
import procureai.reception.client.PurchaseOrderResponse;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReceptionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderClient orderClient;

    @BeforeEach
    void orderExists() {
        when(orderClient.getPurchaseOrderById(1L)).thenReturn(new PurchaseOrderResponse(1L, 7L, "CONFIRMED",
                List.of(new PurchaseOrderResponse.Line(100L, "Laptop", 2))));
    }

    @Test
    void createReception() throws Exception {
        String body = """
                {
                  "purchaseOrderId": 1,
                  "supplierId": 7,
                  "deliveryNoteNumber": "BL-42",
                  "lines": [
                    {"productId": 100, "productName": "Laptop", "quantityReceived": 2, "quantityRejected": 0}
                  ]
                }
                """;

        mockMvc.perform(post("/api/receptions").header("X-User-Role", "MAGASINIER").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lines[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.lines[0].quantityOrdered").value(2));

        mockMvc.perform(get("/api/receptions/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].supplierId").value(7));
    }

    @Test
    void onlyMagasinierAndAdminCanReceive() throws Exception {
        String body = """
                {
                  "purchaseOrderId": 1,
                  "supplierId": 7,
                  "lines": [
                    {"productId": 100, "quantityReceived": 1}
                  ]
                }
                """;

        mockMvc.perform(post("/api/receptions").header("X-User-Role", "EMPLOYEE").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/receptions").header("X-User-Role", "MANAGER").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/receptions").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/receptions").header("X-User-Role", "ADMIN").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void receivingMoreThanOrderedReturns400() throws Exception {
        String body = """
                {
                  "purchaseOrderId": 1,
                  "supplierId": 7,
                  "lines": [
                    {"productId": 100, "quantityReceived": 5}
                  ]
                }
                """;

        mockMvc.perform(post("/api/receptions").header("X-User-Role", "MAGASINIER").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void productNotInOrderReturns400() throws Exception {
        String body = """
                {
                  "purchaseOrderId": 1,
                  "supplierId": 7,
                  "lines": [
                    {"productId": 999, "quantityReceived": 1}
                  ]
                }
                """;

        mockMvc.perform(post("/api/receptions").header("X-User-Role", "MAGASINIER").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void wrongSupplierReturns400() throws Exception {
        String body = """
                {
                  "purchaseOrderId": 1,
                  "supplierId": 99,
                  "lines": [
                    {"productId": 100, "quantityReceived": 1}
                  ]
                }
                """;

        mockMvc.perform(post("/api/receptions").header("X-User-Role", "MAGASINIER").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownOrderReturns404() throws Exception {
        when(orderClient.getPurchaseOrderById(anyLong())).thenThrow(mock(FeignException.NotFound.class));
        String body = """
                {
                  "purchaseOrderId": 42,
                  "supplierId": 7,
                  "lines": [{"productId": 10, "quantityReceived": 1, "quantityRejected": 0}]
                }
                """;

        mockMvc.perform(post("/api/receptions").header("X-User-Role", "MAGASINIER").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownReceptionReturns404() throws Exception {
        mockMvc.perform(get("/api/receptions/999")).andExpect(status().isNotFound());
    }
}
