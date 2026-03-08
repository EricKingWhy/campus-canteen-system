package fun.cyhgraph.dto;

import fun.cyhgraph.entity.DishFlavor;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DishDTO implements Serializable {
    // 【核心修复】统一为 Long，匹配 Entity 和 Service
    private Long id;
    private String name;
    private Long categoryId;
    private BigDecimal price;

    @JsonAlias({ "pic", "image" })
    private String image;

    @JsonAlias({ "detail", "description" })
    private String description;
    private Integer status;
    private List<DishFlavor> flavors = new ArrayList<>();

    // 营养成分闭环扩充
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbohydrates;
    private String mainIngredients;
    private String allergenTags;
}
