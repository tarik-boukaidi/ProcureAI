package procureai.matching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import procureai.matching.client.*;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(properties = "matching.price-tolerance-percent=5")
@AutoConfigureMockMvc
class PriceToleranceTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceClient invoiceClient;

    @MockBean
    private OrderClient orderClient;

    @MockBean
    private ReceptionClient receptionClient;

    @BeforeEach
    void laptopOrderedAt500AndReceived() {
        when(orderClient.getPurchaseOrderById(1L)).thenReturn(new OrderResponse(1L, 7L, "CONFIRMED",
                List.of(new OrderResponse.Line(100L, "Laptop", 1, new BigDecimal("500.00")))));
        when(receptionClient.getReceptionsByOrder(1L)).thenReturn(List.of(
                new ReceptionResponse(1L, 1L, List.of(new ReceptionResponse.Line(100L, 1, 0)))));
        when(invoiceClient.getInvoicesByOrder(1L)).thenReturn(List.of());
    }

    private void invoicedAt(Long invoiceId, String unitPrice) {
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(new InvoiceResponse(invoiceId, "FAC-" + invoiceId, 1L, 7L,
                "RECEIVED", List.of(new InvoiceResponse.Line(100L, "Laptop", 1, new BigDecimal(unitPrice)))));
    }

    @Test
    void priceWithinToleranceIsAccepted() throws Exception {
        invoicedAt(20L, "525.00");

        mockMvc.perform(post("/api/matching/invoices/{id}", 20L).header("X-User-Role", "COMPTABLE"))
                .andExpect(jsonPath("$.status").value("MATCHED"));
    }

    @Test
    void priceAboveToleranceIsRejected() throws Exception {
        invoicedAt(21L, "525.01");

        mockMvc.perform(post("/api/matching/invoices/{id}", 21L).header("X-User-Role", "COMPTABLE"))
                .andExpect(jsonPath("$.status").value("MISMATCH"));
    }
}
