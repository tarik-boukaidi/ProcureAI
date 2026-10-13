package procureai.payment.dto;

import procureai.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
        Long id,
        String reference,
        Long invoiceId,
        Long supplierId,
        BigDecimal amount,
        Payment.Method method,
        LocalDate paymentDate) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getReference(), payment.getInvoiceId(),
                payment.getSupplierId(), payment.getAmount(), payment.getMethod(), payment.getPaymentDate());
    }
}
