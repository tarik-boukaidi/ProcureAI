package procureai.accounting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import procureai.accounting.repository.AccountingEntryRepository;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountingEntryRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private void record(String type, long invoiceId, long supplierId, String amount, String date) throws Exception {
        String json = """
                {"type": "%s", "invoiceId": %d, "supplierId": %d, "amount": %s, "entryDate": "%s"}
                """.formatted(type, invoiceId, supplierId, amount, date);
        mockMvc.perform(post("/api/accounting/entries").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void invoiceEntryUsesPurchasesAndSuppliersAccounts() throws Exception {
        mockMvc.perform(post("/api/accounting/entries").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"INVOICE_RECORDED\", \"invoiceId\": 1, \"supplierId\": 7, \"amount\": 1031.50}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.debitAccount").value("6111"))
                .andExpect(jsonPath("$.creditAccount").value("4411"))
                .andExpect(jsonPath("$.entryDate").exists());
    }

    @Test
    void paymentEntryUsesSuppliersAndBankAccounts() throws Exception {
        mockMvc.perform(post("/api/accounting/entries").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"PAYMENT_RECORDED\", \"invoiceId\": 1, \"supplierId\": 7, \"amount\": 1031.50}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.debitAccount").value("4411"))
                .andExpect(jsonPath("$.creditAccount").value("5141"));
    }

    @Test
    void vatEntryUsesVatAndSuppliersAccounts() throws Exception {
        mockMvc.perform(post("/api/accounting/entries").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"VAT_RECORDED\", \"invoiceId\": 1, \"supplierId\": 7, \"amount\": 206.30}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.debitAccount").value("3455"))
                .andExpect(jsonPath("$.creditAccount").value("4411"));
    }

    @Test
    void callerCannotChooseAccounts() throws Exception {
        mockMvc.perform(post("/api/accounting/entries").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"INVOICE_RECORDED\", \"invoiceId\": 1, \"supplierId\": 7, \"amount\": 100, \"debitAccount\": \"999\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.debitAccount").value("6111"));
    }

    @Test
    void sameEntryIsNotRecordedTwice() throws Exception {
        record("INVOICE_RECORDED", 1, 7, "1031.50", "2026-10-01");
        record("INVOICE_RECORDED", 1, 7, "1031.50", "2026-10-01");

        mockMvc.perform(get("/api/accounting/entries/invoice/1").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void invalidEntryReturns400() throws Exception {
        mockMvc.perform(post("/api/accounting/entries").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"INVOICE_RECORDED\", \"invoiceId\": 1, \"supplierId\": 7, \"amount\": 0}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/accounting/entries").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"invoiceId\": 1, \"supplierId\": 7, \"amount\": 100}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void supplierBalanceIsInvoicedWithVatMinusPaid() throws Exception {
        record("INVOICE_RECORDED", 1, 7, "1000.00", "2026-10-01");
        record("VAT_RECORDED", 1, 7, "200.00", "2026-10-01");
        record("INVOICE_RECORDED", 2, 7, "500.00", "2026-10-02");
        record("VAT_RECORDED", 2, 7, "100.00", "2026-10-02");
        record("PAYMENT_RECORDED", 1, 7, "1200.00", "2026-10-05");
        record("INVOICE_RECORDED", 3, 8, "200.00", "2026-10-03");

        mockMvc.perform(get("/api/accounting/suppliers/7/balance").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiced").value(1800.00))
                .andExpect(jsonPath("$.paid").value(1200.00))
                .andExpect(jsonPath("$.remaining").value(600.00));

        mockMvc.perform(get("/api/accounting/outstanding").header("X-User-Role", "ADMIN"))
                .andExpect(jsonPath("$.remaining").value(800.00));
    }

    @Test
    void monthlyReportGroupsInvoicesByMonth() throws Exception {
        record("INVOICE_RECORDED", 1, 7, "100.00", "2026-01-15");
        record("INVOICE_RECORDED", 2, 7, "50.00", "2026-01-20");
        record("INVOICE_RECORDED", 3, 7, "300.00", "2026-03-10");
        record("VAT_RECORDED", 1, 7, "20.00", "2026-01-15");
        record("PAYMENT_RECORDED", 1, 7, "120.00", "2026-01-25");
        record("INVOICE_RECORDED", 4, 7, "999.00", "2025-01-10");

        mockMvc.perform(get("/api/accounting/report").param("year", "2026").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)))
                .andExpect(jsonPath("$[0].total").value(150.00))
                .andExpect(jsonPath("$[1].total").value(0))
                .andExpect(jsonPath("$[2].total").value(300.00));
    }

    @Test
    void onlyComptableAndAdminCanReadAccounting() throws Exception {
        mockMvc.perform(get("/api/accounting/entries").header("X-User-Role", "COMPTABLE"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/accounting/entries").header("X-User-Role", "MANAGER"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/accounting/outstanding"))
                .andExpect(status().isForbidden());
    }
}
