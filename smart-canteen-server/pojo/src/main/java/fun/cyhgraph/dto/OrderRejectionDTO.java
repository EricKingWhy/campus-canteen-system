package fun.cyhgraph.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrderRejectionDTO implements Serializable {
    private Long id; // 【修复】Integer -> Long 支持大订单号
    private String rejectionReason;
}
