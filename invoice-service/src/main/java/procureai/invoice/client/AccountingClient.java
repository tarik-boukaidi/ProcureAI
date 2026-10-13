package procureai.invoice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "accounting-service")
public interface AccountingClient {

    @PostMapping("/api/accounting/entries")
    void record(@RequestBody AccountingEntryRequest request);
}
