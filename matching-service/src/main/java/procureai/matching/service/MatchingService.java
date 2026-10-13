package procureai.matching.service;

import procureai.matching.dto.MatchResultResponse;
import procureai.matching.entity.MatchResult;

import java.util.List;

public interface MatchingService {

    MatchResultResponse matchInvoice(Long invoiceId);

    MatchResultResponse getMatchResult(Long id);

    List<MatchResultResponse> getAllMatchResults();

    List<MatchResultResponse> getMatchResultsByInvoice(Long invoiceId);

    List<MatchResultResponse> getMatchResultsByStatus(MatchResult.Status status);
}
