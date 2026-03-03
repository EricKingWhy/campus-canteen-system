package fun.cyhgraph.vo;

import fun.cyhgraph.entity.DishFlavor;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishVO implements Serializable {
    private Long id;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer status;
    private LocalDateTime updateTime;
    private String categoryName;
    private List<DishFlavor> flavors = new ArrayList<>();
    private Integer copies;

    // 营养成分
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbohydrates;

    // 食材/过敏原标签
    private String allergenTags;

    // 销量
    private Integer sold;
}
