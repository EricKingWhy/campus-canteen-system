package fun.cyhgraph.entity;

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
public class Dish implements Serializable {
    private Long id;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer status;

    // 【核心修复】补全排序字段，解决 getSort() 找不到的报错
    private Integer sort;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;

    // 营养成分
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbohydrates;

    // 食材/过敏原标签 (逗号分隔，如 "海鲜,花生,乳制品")
    private String allergenTags;

    // 销量字段
    private Integer sold;
}
