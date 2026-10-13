package procureai.matching.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@FeignClient(name = "invoice-service")
public interface InvoiceClient {

    @GetMapping("/api/invoices/{id}")
    InvoiceResponse getInvoiceById(@PathVariable("id") Long id);

    @GetMapping("/api/invoices/order/{purchaseOrderId}")
    List<InvoiceResponse> getInvoicesByOrder(@PathVariable("purchaseOrderId") Long purchaseOrderId);

    @PutMapping("/api/invoices/{id}/validate")
    InvoiceResponse validateInvoice(@PathVariable("id") Long id);

    @PutMapping("/api/invoices/{id}/reject")
    InvoiceResponse rejectInvoice(@PathVariable("id") Long id);
}
