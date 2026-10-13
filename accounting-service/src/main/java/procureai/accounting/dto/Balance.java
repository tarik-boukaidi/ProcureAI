package procureai.accounting.dto;

import java.math.BigDecimal;

// remaining = ce qu'on doit encore = facture - paye
public record Balance(BigDecimal invoiced, BigDecimal paid, BigDecimal remaining) {
}
