package fun.cyhgraph.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cart") // 【核心修复】映射数据库表名 cart
public class ShoppingCart implements Serializable {
    private Long id;
    private String name;

    @TableField("pic") // 【核心修复】映射图片字段 pic
    private String image;

    private Long userId;
    private Long dishId;
    private Long setmealId;
    private String dishFlavor;
    private Integer number;
    private BigDecimal amount;
    private LocalDateTime createTime;
}
