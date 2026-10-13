package procureai.demande.dto;

import procureai.demande.entity.PurchaseRequest;
import procureai.demande.entity.PurchaseRequest.Status;
import procureai.demande.entity.PurchaseRequestLine;

import java.util.List;

public record DemandeResponse(Long id, String reason, Long requesterId, Status status, List<Item> items) {

    public record Item(Long id, String product, int quantity) {

        static Item from(PurchaseRequestLine line) {
            return new Item(line.getId(), line.getProduct(), line.getQuantity());
        }
    }

    public static DemandeResponse from(PurchaseRequest demande) {
        return new DemandeResponse(demande.getId(), demande.getReason(), demande.getRequesterId(), demande.getStatus(),
                demande.getItems().stream().map(Item::from).toList());
    }
}
