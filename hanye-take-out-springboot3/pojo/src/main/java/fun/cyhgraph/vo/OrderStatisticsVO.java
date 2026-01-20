package fun.cyhgraph.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrderStatisticsVO implements Serializable {
    private Integer toBeConfirmed; // 待接单
    private Integer confirmed; // 待派送
    private Integer deliveryInProgress; // 派送中
}
