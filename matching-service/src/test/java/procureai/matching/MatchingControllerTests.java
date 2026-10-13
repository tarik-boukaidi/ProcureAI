package procureai.matching;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import procureai.matching.client.*;
import procureai.matching.repository.MatchResultRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MatchingControllerTests {

    private static final Long INVOICE_ID = 10L;
    private static final Long ORDER_ID = 1L;
    private static final Long SUPPLIER_ID = 7L;
    private static final Long LAPTOP = 100L;
    private static final Long MOUSE = 101L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceClient invoiceClient;

    @MockBean
    private OrderClient orderClient;

    @MockBean
    private ReceptionClient receptionClient;

    @Autowired
    private MatchResultRepository repository;

    @BeforeEach
    void orderOfTwoLaptopsAndThreeMice() {
        repository.deleteAll();
        when(orderClient.getPurchaseOrderById(ORDER_ID)).thenReturn(new OrderResponse(ORDER_ID, SUPPLIER_ID, "CONFIRMED", List.of(
                new OrderResponse.Line(LAPTOP, "Laptop", 2, new BigDecimal("500.00")),
                new OrderResponse.Line(MOUSE, "Mouse", 3, new BigDecimal("10.50")))));
        when(invoiceClient.getInvoicesByOrder(ORDER_ID)).thenReturn(List.of());
    }

    private void received(int laptops, int rejectedLaptops, int mice) {
        when(receptionClient.getReceptionsByOrder(ORDER_ID)).thenReturn(List.of(new ReceptionResponse(1L, ORDER_ID, List.of(
                new ReceptionResponse.Line(LAPTOP, laptops, rejectedLaptops),
                new ReceptionResponse.Line(MOUSE, mice, 0)))));
    }

    private void invoiceIs(InvoiceResponse.Line... lines) {
        when(invoiceClient.getInvoiceById(INVOICE_ID)).thenReturn(
                new InvoiceResponse(INVOICE_ID, "FAC-1", ORDER_ID, SUPPLIER_ID, "RECEIVED", List.of(lines)));
    }

    private static InvoiceResponse.Line line(Long productId, int quantity, String unitPrice) {
        return new InvoiceResponse.Line(productId, null, quantity, new BigDecimal(unitPrice));
    }

    @Test
    void perfectMatchValidatesInvoice() throws Exception {
        received(2, 0, 3);
        invoiceIs(line(LAPTOP, 2, "500.00"), line(MOUSE, 3, "10.50"));

        mockMvc.perform(post("/api/matching/invoices/{id}", INVOICE_ID).header("X-User-Role", "COMPTABLE"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("MATCHED"))
                .andExpect(jsonPath("$.discrepancies", hasSize(0)));

        verify(invoiceClient).validateInvoice(INVOICE_ID);
        verify(invoiceClient, never()).rejectInvoice(anyLong());
    }

    @Test
    void priceDifferenceRejectsInvoice() throws Exception {
        received(2, 0, 3);
        invoiceIs(line(LAPTOP, 2, "550.00"));

        mockMvc.perform(post("/api/matching/invoices/{id}", INVOICE_ID).header("X-User-Role", "COMPTABLE"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("MISMATCH"))
                .andExpect(jsonPath("$.discrepancies[0].type").value("PRICE_MISMATCH"))
                .andExpect(jsonPath("$.discrepancies[0].expectedValue").value("500.00"))
                .andExpect(jsonPath("$.discrepancies[0].actualValue").value("550.00"));

        verify(invoiceClient).rejectInvoice(INVOICE_ID);
        verify(invoiceClient, never()).validateInvoice(anyLong());
    }

    @Test
    void invoicingMoreThanAcceptedQuantityRejectsInvoice() throws Exception {
        received(2, 1, 3);
        invoiceIs(line(LAPTOP, 2, "500.00"));

        mockMvc.perform(post("/api/matching/invoices/{id}", INVOICE_ID).header("X-User-Role", "COMPTABLE"))
                .andExpect(jsonPath("$.status").value("MISMATCH"))
                .andExpect(jsonPath("$.discrepancies[0].type").value("QUANTITY_EXCEEDS_RECEIVED"))
                .andExpect(jsonPath("$.discrepancies[0].expectedValue").value("1"))
                .andExpect(jsonPath("$.discrepancies[0].actualValue").value("2"));

        verify(invoiceClient).rejectInvoice(INVOICE_ID);
    }

    @Test
    void quantityAlreadyInvoicedCannotBeInvoicedTwice() throws Exception {
        received(2, 0, 3);
        invoiceIs(line(LAPTOP, 2, "500.00"));
        when(invoiceClient.getInvoicesByOrder(ORDER_ID)).thenReturn(List.of(
                new InvoiceResponse(9L, "FAC-0", ORDER_ID, SUPPLIER_ID, "VALIDATED", List.of(line(LAPTOP, 2, "500.00")))));

        mockMvc.perform(post("/api/matching/invoices/{id}", INVOICE_ID).header("X-User-Role", "COMPTABLE"))
                .andExpect(jsonPath("$.status").value("MISMATCH"))
                .andExpect(jsonPath("$.discrepancies[0].type").value("QUANTITY_EXCEEDS_RECEIVED"));

        verify(invoiceClient).rejectInvoice(INVOICE_ID);
    }

    @Test
    void productNotOrderedRejectsInvoice() throws Exception {
        received(2, 0, 3);
        invoiceIs(line(999L, 1, "20.00"));

        mockMvc.perform(post("/api/matching/invoices/{id}", INVOICE_ID).header("X-User-Role", "COMPTABLE"))
                .andExpect(jsonPath("$.status").value("MISMATCH"))
                .andExpect(jsonPath("$.discrepancies[0].type").value("PRODUCT_NOT_ORDERED"));
    }

    @Test
    void alreadyProcessedInvoiceReturns409() throws Exception {
        when(invoiceClient.getInvoiceById(INVOICE_ID)).thenReturn(
                new InvoiceResponse(INVOICE_ID, "FAC-1", ORDER_ID, SUPPLIER_ID, "VALIDATED", List.of()));

        mockMvc.perform(post("/api/matching/invoices/{id}", INVOICE_ID).header("X-User-Role", "COMPTABLE"))
                .andExpect(status().isConflict());

        verify(invoiceClient, never()).validateInvoice(anyLong());
        verify(invoiceClient, never()).rejectInvoice(anyLong());
    }

    @Test
    void onlyComptableCanRunMatching() throws Exception {
        received(2, 0, 3);
        invoiceIs(line(LAPTOP, 2, "500.00"));

        mockMvc.perform(post("/api/matching/invoices/{id}", INVOICE_ID).header("X-User-Role", "EMPLOYEE"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/matching/invoices/{id}", INVOICE_ID).header("X-User-Role", "ADMIN"))
                .andExpect(status().isForbidden());

        verify(invoiceClient, never()).validateInvoice(anyLong());
        verify(invoiceClient, never()).rejectInvoice(anyLong());
    }

    @Test
    void unknownInvoiceReturns404() throws Exception {
        when(invoiceClient.getInvoiceById(anyLong())).thenThrow(mock(FeignException.NotFound.class));

        mockMvc.perform(post("/api/matching/invoices/{id}", 999L).header("X-User-Role", "COMPTABLE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void matchHistoryIsStoredPerInvoice() throws Exception {
        received(2, 0, 3);
        invoiceIs(line(MOUSE, 3, "10.50"));

        mockMvc.perform(post("/api/matching/invoices/{id}", INVOICE_ID).header("X-User-Role", "COMPTABLE")).andExpect(status().isCreated());

        mockMvc.perform(get("/api/matching/invoice/{id}", INVOICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("MATCHED"));
    }
}
