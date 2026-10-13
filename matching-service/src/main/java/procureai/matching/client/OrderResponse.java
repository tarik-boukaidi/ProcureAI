package procureai.matching.client;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(Long id, Long supplierId, String status, List<Line> lines) {

    public record Line(Long productId, String productName, int quantity, BigDecimal unitPrice) {
    }
}
