package procureai.payment.dto;

import jakarta.validation.constraints.NotNull;
import procureai.payment.entity.Payment;

import java.time.LocalDate;

public record CreatePaymentRequest(@NotNull Long invoiceId, @NotNull Payment.Method method, LocalDate paymentDate) {
}
