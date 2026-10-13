package procureai.matching.dto;

import procureai.matching.entity.Discrepancy;
import procureai.matching.entity.MatchResult;

import java.time.LocalDateTime;
import java.util.List;

public record MatchResultResponse(
        Long id,
        Long invoiceId,
        Long purchaseOrderId,
        MatchResult.Status status,
        LocalDateTime checkedAt,
        List<Line> discrepancies) {

    public record Line(Long id, Discrepancy.Type type, Long productId, String expectedValue, String actualValue) {

        static Line from(Discrepancy discrepancy) {
            return new Line(discrepancy.getId(), discrepancy.getType(), discrepancy.getProductId(),
                    discrepancy.getExpectedValue(), discrepancy.getActualValue());
        }
    }

    public static MatchResultResponse from(MatchResult result) {
        return new MatchResultResponse(result.getId(), result.getInvoiceId(), result.getPurchaseOrderId(),
                result.getStatus(), result.getCheckedAt(),
                result.getDiscrepancies().stream().map(Line::from).toList());
    }
}
