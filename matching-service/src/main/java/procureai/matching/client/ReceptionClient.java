package procureai.matching.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "reception-service")
public interface ReceptionClient {

    @GetMapping("/api/receptions/order/{purchaseOrderId}")
    List<ReceptionResponse> getReceptionsByOrder(@PathVariable("purchaseOrderId") Long purchaseOrderId);
}
