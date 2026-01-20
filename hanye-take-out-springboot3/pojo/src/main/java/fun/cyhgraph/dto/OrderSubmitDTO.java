package fun.cyhgraph.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderSubmitDTO implements Serializable {
    private Integer addressBookId;
    private int payMethod;
    private String remark;
    private LocalDateTime estimatedDeliveryTime;
    private Integer deliveryStatus;
    private Integer packAmount;
    private Integer tablewareNumber;
    private Integer tablewareStatus;
    private BigDecimal amount; // 总金额
}