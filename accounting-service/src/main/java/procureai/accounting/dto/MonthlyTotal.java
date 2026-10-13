package procureai.accounting.dto;

import java.math.BigDecimal;

public record MonthlyTotal(int month, BigDecimal total) {
}
