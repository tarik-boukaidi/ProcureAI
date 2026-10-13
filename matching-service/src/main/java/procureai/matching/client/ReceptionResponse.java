package procureai.matching.client;

import java.util.List;

public record ReceptionResponse(Long id, Long purchaseOrderId, List<Line> lines) {

    public record Line(Long productId, int quantityReceived, int quantityRejected) {

        public int acceptedQuantity() {
            return quantityReceived - quantityRejected;
        }
    }
}
