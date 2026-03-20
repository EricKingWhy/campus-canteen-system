package fun.cyhgraph.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportVO implements Serializable {
    private BigDecimal totalAmount;
    private Integer totalOrders;
    private BigDecimal dailyAverage;
    private List<DailyTrendVO> dailyTrend;
    private List<MealPeriodVO> mealPeriodDistribution;
    private String maxSpendDish;
    private BigDecimal maxSpendAmount;
    private String mostFrequentDish;
}
