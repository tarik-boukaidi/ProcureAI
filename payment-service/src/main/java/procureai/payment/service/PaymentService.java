package procureai.payment.service;

import procureai.payment.client.InvoiceResponse;
import procureai.payment.dto.CreatePaymentRequest;
import procureai.payment.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse payInvoice(CreatePaymentRequest request);

    PaymentResponse getPayment(Long id);

    List<PaymentResponse> getAllPayments();

    PaymentResponse getPaymentByInvoice(Long invoiceId);

    List<PaymentResponse> getPaymentsBySupplier(Long supplierId);

    List<InvoiceResponse> getInvoicesToPay();
}
