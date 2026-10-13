package procureai.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@FeignClient(name = "invoice-service")
public interface InvoiceClient {

    @GetMapping("/api/invoices/{id}")
    InvoiceResponse getInvoiceById(@PathVariable("id") Long id);

    @GetMapping("/api/invoices/status/{status}")
    List<InvoiceResponse> getInvoicesByStatus(@PathVariable("status") String status);

    @PutMapping("/api/invoices/{id}/pay")
    InvoiceResponse markAsPaid(@PathVariable("id") Long id);
}
