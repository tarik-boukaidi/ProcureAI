package procureai.reception.dto;

import procureai.reception.entity.Reception;
import procureai.reception.entity.ReceptionLine;

import java.time.LocalDate;
import java.util.List;

public record ReceptionResponse(
        Long id,
        Long purchaseOrderId,
        Long supplierId,
        String deliveryNoteNumber,
        LocalDate receivedDate,
        String comment,
        List<Line> lines) {

    public record Line(Long id, Long productId, String productName,
                       int quantityOrdered, int quantityReceived, int quantityRejected) {

        static Line from(ReceptionLine line) {
            return new Line(line.getId(), line.getProductId(), line.getProductName(),
                    line.getQuantityOrdered(), line.getQuantityReceived(), line.getQuantityRejected());
        }
    }

    public static ReceptionResponse from(Reception reception) {
        return new ReceptionResponse(reception.getId(), reception.getPurchaseOrderId(), reception.getSupplierId(),
                reception.getDeliveryNoteNumber(), reception.getReceivedDate(), reception.getComment(),
                reception.getLines().stream().map(Line::from).toList());
    }
}
