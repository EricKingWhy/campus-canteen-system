package fun.cyhgraph.dto;

import fun.cyhgraph.entity.DishFlavor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DishDTO implements Serializable {

    private Integer id;
    private String name;
    private String pic;

    // --- 新增营养分析字段 (开始) ---
    private Double calories; // 热量(大卡)
    private Double protein; // 蛋白质(克)
    private Double fat; //
    private Double carbohydrates; // 脂肪(克)
    private Double carbonWater; // 碳水化合物(克)
    private String mainIngredients; //
    private String stallName; //
    private String nutritionTags; //
    // --- 新增营养分析字段 (结束) ---

    private String detail;
    private BigDecimal price;
    private String status;
    private Integer categoryId;
    // 多种口味，包括温度，忌口等(每种口味又对应一个列表)，且数据在口味表中而不是在Dish里，口味表有外键关联Dish
    private List<DishFlavor> flavors = new ArrayList<>();

}
