package procureai.payment;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import procureai.payment.client.InvoiceClient;
import procureai.payment.client.InvoiceResponse;
import procureai.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTests {

    private static final String PAY_INVOICE_10 = """
            {"invoiceId": 10, "method": "BANK_TRANSFER", "paymentDate": "2026-10-15"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository repository;

    @MockBean
    private InvoiceClient invoiceClient;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private void invoiceIs(String status) {
        when(invoiceClient.getInvoiceById(10L)).thenReturn(new InvoiceResponse(
                10L, "FAC-1", 7L, status, new BigDecimal("1031.50"), new BigDecimal("1237.80"), LocalDate.of(2026, 10, 31)));
    }

    @Test
    void payValidatedInvoice() throws Exception {
        invoiceIs("VALIDATED");

        mockMvc.perform(post("/api/payments").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(PAY_INVOICE_10))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference", startsWith("PAY-")))
                // On paie le TTC, pas le HT
                .andExpect(jsonPath("$.amount").value(1237.80))
                .andExpect(jsonPath("$.supplierId").value(7))
                .andExpect(jsonPath("$.paymentDate").value("2026-10-15"));

        verify(invoiceClient).markAsPaid(10L);

        mockMvc.perform(get("/api/payments/invoice/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method").value("BANK_TRANSFER"));
    }

    @Test
    void invoiceNotValidatedCannotBePaid() throws Exception {
        invoiceIs("RECEIVED");

        mockMvc.perform(post("/api/payments").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(PAY_INVOICE_10))
                .andExpect(status().isConflict());

        verify(invoiceClient, never()).markAsPaid(anyLong());
    }

    @Test
    void invoiceCannotBePaidTwice() throws Exception {
        invoiceIs("VALIDATED");
        mockMvc.perform(post("/api/payments").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(PAY_INVOICE_10))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/payments").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(PAY_INVOICE_10))
                .andExpect(status().isConflict());

        verify(invoiceClient, times(1)).markAsPaid(10L);
    }

    @Test
    void onlyComptableCanPay() throws Exception {
        invoiceIs("VALIDATED");

        mockMvc.perform(post("/api/payments").header("X-User-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON).content(PAY_INVOICE_10))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/payments").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content(PAY_INVOICE_10))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(PAY_INVOICE_10))
                .andExpect(status().isForbidden());

        verify(invoiceClient, never()).markAsPaid(anyLong());
    }

    @Test
    void unknownInvoiceReturns404() throws Exception {
        when(invoiceClient.getInvoiceById(anyLong())).thenThrow(mock(FeignException.NotFound.class));

        mockMvc.perform(post("/api/payments").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content(PAY_INVOICE_10))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingMethodReturns400() throws Exception {
        mockMvc.perform(post("/api/payments").header("X-User-Role", "COMPTABLE").contentType(MediaType.APPLICATION_JSON).content("{\"invoiceId\": 10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invoicesToPayAreSortedByDueDate() throws Exception {
        when(invoiceClient.getInvoicesByStatus("VALIDATED")).thenReturn(List.of(
                new InvoiceResponse(1L, "FAC-LATE", 7L, "VALIDATED", BigDecimal.TEN, BigDecimal.TEN, LocalDate.of(2026, 12, 1)),
                new InvoiceResponse(2L, "FAC-URGENT", 7L, "VALIDATED", BigDecimal.TEN, BigDecimal.TEN, LocalDate.of(2026, 10, 20))));

        mockMvc.perform(get("/api/payments/to-pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoiceNumber").value("FAC-URGENT"))
                .andExpect(jsonPath("$[1].invoiceNumber").value("FAC-LATE"));
    }

    @Test
    void unknownPaymentReturns404() throws Exception {
        mockMvc.perform(get("/api/payments/999")).andExpect(status().isNotFound());
    }
}
