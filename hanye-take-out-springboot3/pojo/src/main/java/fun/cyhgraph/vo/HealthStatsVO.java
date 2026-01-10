package fun.cyhgraph.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HealthStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double bmi;
    private String bmiStatus;
    private Double targetCalories;
    private String suggestion;
}
