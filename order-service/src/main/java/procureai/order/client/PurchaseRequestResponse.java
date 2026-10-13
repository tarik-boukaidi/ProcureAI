package procureai.order.client;

import java.util.List;

public record PurchaseRequestResponse(Long id, Long requesterId, String status, List<Item> items) {

    public record Item(String product, int quantity) {
    }
}
