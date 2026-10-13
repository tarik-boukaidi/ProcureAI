package procureai.invoice;

import com.jayway.jsonpath.JsonPath;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import procureai.invoice.client.AccountingClient;
import procureai.invoice.client.AccountingEntryRequest;
import procureai.invoice.client.OrderClient;
import procureai.invoice.client.PurchaseOrderResponse;
import procureai.invoice.messaging.NotificationPublisher;
import procureai.invoice.messaging.NotificationRequest;
import procureai.invoice.repository.InvoiceRepository;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InvoiceControllerTests {

    private static final String INVOICE_BODY = """
            {
              "invoiceNumber": "FAC-2026-001",
              "purchaseOrderId": 1,
              "supplierId": 7,
              "invoiceDate": "2026-10-01",
              "lines": [
                {"productId": 10, "productName": "Laptop", "quantity": 2, "unitPrice": 500.00},
                {"productId": 11, "productName": "Mouse", "quantity": 3, "unitPrice": 10.50}
              ]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvoiceRepository repository;

    @MockBean
    private OrderClient orderClient;

    @MockBean
    private NotificationPublisher notificationPublisher;

    @MockBean
    private AccountingClient accountingClient;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private void orderIs(String status, Long supplierId) {
        when(orderClient.getPurchaseOrderById(1L))
                .thenReturn(new PurchaseOrderResponse(1L, "PO-1", supplierId, status, new BigDecimal("1031.50")));
    }

    @Test
    void createInvoiceComputesTotalAndDueDate() throws Exception {
        orderIs("CONFIRMED", 7L);

        mockMvc.perform(post("/api/invoices").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(INVOICE_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.totalAmount").value(1031.50))
                .andExpect(jsonPath("$.vatRate").value(20))
                .andExpect(jsonPath("$.vatAmount").value(206.30))
                .andExpect(jsonPath("$.totalTtc").value(1237.80))
                .andExpect(jsonPath("$.dueDate").value("2026-10-31"))
                .andExpect(jsonPath("$.lines", hasSize(2)));

        mockMvc.perform(get("/api/invoices/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void duplicateInvoiceNumberReturns409() throws Exception {
        orderIs("CONFIRMED", 7L);

        mockMvc.perform(post("/api/invoices").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(INVOICE_BODY))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/invoices").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(INVOICE_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void wrongSupplierReturns400() throws Exception {
        orderIs("CONFIRMED", 99L);

        mockMvc.perform(post("/api/invoices").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(INVOICE_BODY))
                .andExpect(status().isBadRequest());
    }

    @Test
    void draftOrderCannotBeInvoiced() throws Exception {
        orderIs("DRAFT", 7L);

        mockMvc.perform(post("/api/invoices").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(INVOICE_BODY))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownOrderReturns404() throws Exception {
        when(orderClient.getPurchaseOrderById(anyLong())).thenThrow(mock(FeignException.NotFound.class));

        mockMvc.perform(post("/api/invoices").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(INVOICE_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void invoiceLifecycle() throws Exception {
        orderIs("DELIVERED", 7L);

        String created = mockMvc.perform(post("/api/invoices").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(INVOICE_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(created, "$.id");

        mockMvc.perform(put("/api/invoices/{id}/pay", id)).andExpect(status().isConflict());

        mockMvc.perform(put("/api/invoices/{id}/validate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALIDATED"));

        mockMvc.perform(put("/api/invoices/{id}/pay", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(notificationPublisher).send(new NotificationRequest(null, "COMPTABLE", "INVOICE_PAID",
                "Facture FAC-2026-001 payee : 1237.80"));

        // Validation : achat HT + TVA. Paiement : le TTC.
        Long invoiceId = id.longValue();
        verify(accountingClient).record(new AccountingEntryRequest(
                "INVOICE_RECORDED", invoiceId, 7L, new BigDecimal("1031.50"), "Facture FAC-2026-001 a payer"));
        verify(accountingClient).record(new AccountingEntryRequest(
                "VAT_RECORDED", invoiceId, 7L, new BigDecimal("206.30"), "TVA facture FAC-2026-001"));
        verify(accountingClient).record(new AccountingEntryRequest(
                "PAYMENT_RECORDED", invoiceId, 7L, new BigDecimal("1237.80"), "Paiement facture FAC-2026-001"));
    }

    @Test
    void vatRateMustBeAMoroccanRate() throws Exception {
        orderIs("CONFIRMED", 7L);
        String body = INVOICE_BODY.replace("\"supplierId\": 7,", "\"supplierId\": 7, \"vatRate\": 14,");
        mockMvc.perform(post("/api/invoices").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vatAmount").value(144.41))
                .andExpect(jsonPath("$.totalTtc").value(1175.91));

        String wrongRate = INVOICE_BODY.replace("\"supplierId\": 7,", "\"supplierId\": 7, \"vatRate\": 19,")
                .replace("FAC-2026-001", "FAC-2026-002");
        mockMvc.perform(post("/api/invoices").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(wrongRate))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validationIsCancelledIfAccountingIsDown() throws Exception {
        orderIs("DELIVERED", 7L);
        doThrow(mock(FeignException.class)).when(accountingClient).record(any());

        String created = mockMvc.perform(post("/api/invoices").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(INVOICE_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(created, "$.id");

        mockMvc.perform(put("/api/invoices/{id}/validate", id))
                .andExpect(status().isServiceUnavailable());

        mockMvc.perform(get("/api/invoices/{id}", id))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void onlyComptableCreatesInvoices() throws Exception {
        orderIs("CONFIRMED", 7L);

        mockMvc.perform(post("/api/invoices").header("X-User-Role", "ADMIN").contentType(MediaType.APPLICATION_JSON).content(INVOICE_BODY))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/invoices").contentType(MediaType.APPLICATION_JSON).content(INVOICE_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void statusCannotBeChangedFromOutside() throws Exception {
        orderIs("DELIVERED", 7L);

        String created = mockMvc.perform(post("/api/invoices").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(INVOICE_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(created, "$.id");

        // Un utilisateur passe par le gateway (header X-User-Role) : meme le comptable doit lancer le rapprochement
        mockMvc.perform(put("/api/invoices/{id}/validate", id).header("X-User-Role", "COMPTABLE")).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/invoices/{id}/reject", id).header("X-User-Role", "ADMIN")).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/invoices/{id}/pay", id).header("X-User-Role", "COMPTABLE")).andExpect(status().isForbidden());

        mockMvc.perform(get("/api/invoices/{id}", id))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void rejectInvoiceNotifiesComptables() throws Exception {
        orderIs("DELIVERED", 7L);

        String created = mockMvc.perform(post("/api/invoices").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(INVOICE_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(created, "$.id");

        mockMvc.perform(put("/api/invoices/{id}/reject", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(notificationPublisher).send(new NotificationRequest(null, "COMPTABLE", "INVOICE_REJECTED",
                "Facture FAC-2026-001 rejetee"));
    }

    @Test
    void unknownInvoiceReturns404() throws Exception {
        mockMvc.perform(get("/api/invoices/999")).andExpect(status().isNotFound());
    }
}
