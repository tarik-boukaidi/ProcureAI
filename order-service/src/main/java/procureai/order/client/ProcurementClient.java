package procureai.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "procurement-service")
public interface ProcurementClient {

    @GetMapping("/api/demandes/{id}")
    PurchaseRequestResponse getDemandeById(@PathVariable("id") Long id);
}