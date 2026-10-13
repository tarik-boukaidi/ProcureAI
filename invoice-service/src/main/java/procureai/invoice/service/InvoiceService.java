package procureai.invoice.service;

import procureai.invoice.dto.InvoiceRequest;
import procureai.invoice.dto.InvoiceResponse;
import procureai.invoice.entity.Invoice;

import java.util.List;

public interface InvoiceService {

    InvoiceResponse createInvoice(InvoiceRequest request);

    InvoiceResponse getInvoiceById(Long id);

    List<InvoiceResponse> getAllInvoices();

    List<InvoiceResponse> getInvoicesByPurchaseOrder(Long purchaseOrderId);

    List<InvoiceResponse> getInvoicesBySupplier(Long supplierId);

    List<InvoiceResponse> getInvoicesByStatus(Invoice.Status status);

    InvoiceResponse validateInvoice(Long id);

    InvoiceResponse rejectInvoice(Long id);

    InvoiceResponse markAsPaid(Long id);
}
