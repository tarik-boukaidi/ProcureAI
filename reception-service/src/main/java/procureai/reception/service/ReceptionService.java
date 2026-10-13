package procureai.reception.service;

import procureai.reception.dto.ReceptionRequest;
import procureai.reception.dto.ReceptionResponse;

import java.util.List;

public interface ReceptionService {
    ReceptionResponse createReception(ReceptionRequest request);
    ReceptionResponse getReceptionById(Long id);
    List<ReceptionResponse> getAllReceptions();
    List<ReceptionResponse> getReceptionsByOrder(Long purchaseOrderId);
}
