package fun.cyhgraph.vo;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class MealPeriodVO implements Serializable {
    private String mealPeriod;
    private BigDecimal percentage;
}
