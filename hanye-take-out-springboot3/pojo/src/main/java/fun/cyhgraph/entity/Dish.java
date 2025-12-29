package fun.cyhgraph.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dish implements Serializable {

    private Integer id;
    private String name;
    private String pic;

    // --- 新增营养分析字段 (开始) ---
    private Integer calories; // 热量(大卡)
    private Double protein; // 蛋白质(克)
    private Double fat; // 脂肪(克)
    private Double carbonWater; // 碳水化合物(克)
    private String mainIngredients; // 主要成分
    // --- 新增营养分析字段 (结束) ---


    private String detail;
    private BigDecimal price;
    private Integer status;
    private Integer categoryId;
    private Integer createUser;
    private Integer updateUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
