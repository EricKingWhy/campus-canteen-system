package fun.cyhgraph.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrderDTO implements Serializable {
    private Integer id;
    private String rejectionReason; // 拒单原因
    private String cancelReason; // 取消原因
}
