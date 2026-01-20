package fun.cyhgraph.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorite implements Serializable {
    private Long id;
    // 【核心修复】统一为 Long
    private Long userId;
    private Long dishId;
    private Long setmealId;
    private LocalDateTime createTime;
}
