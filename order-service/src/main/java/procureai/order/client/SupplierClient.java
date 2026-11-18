package procureai.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "supplier-service")
public interface SupplierClient {

    @GetMapping("/api/suppliers/{id}")
    SupplierInfo getSupplier(@PathVariable("id") Long id);
}
