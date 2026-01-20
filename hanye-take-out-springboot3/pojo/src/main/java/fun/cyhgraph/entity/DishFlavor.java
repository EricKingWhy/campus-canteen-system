package fun.cyhgraph.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishFlavor implements Serializable {
    private Long id;
    // 【核心修复】统一为 Long，匹配 Dish 的 ID 类型
    private Long dishId;
    private String name;
    private String value;
}
