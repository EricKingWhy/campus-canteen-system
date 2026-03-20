package fun.cyhgraph.vo;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DailyTrendVO implements Serializable {
    private String day;
    private BigDecimal dailyTotal;
}
