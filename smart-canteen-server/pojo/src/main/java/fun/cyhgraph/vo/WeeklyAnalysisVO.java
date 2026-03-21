package fun.cyhgraph.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAnalysisVO implements Serializable {
    private BigDecimal maxAmount;
    private String maxDishName;
    private String topDishName;
    private Integer topDishCount;
    private Double avgIntervalHours;
}

