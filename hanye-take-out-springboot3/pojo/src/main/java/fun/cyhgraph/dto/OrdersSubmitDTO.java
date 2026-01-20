package fun.cyhgraph.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrdersSubmitDTO implements Serializable {
    // 允许为空（食堂模式）
    private Long addressBookId;
    private Integer payMethod;
    private String remark;
    private BigDecimal amount;

    // 附加字段
    private LocalDateTime estimatedDeliveryTime;
    private Integer deliveryStatus;
    private Integer packAmount;
    private Integer tablewareNumber;
    private Integer tablewareStatus;

    // 【核心新增】直接传递地址信息 (无需 addressBookId)
    private String address;
    private String consignee;
    private String phone;
}
