package fun.cyhgraph.dto;

import fun.cyhgraph.entity.SetmealDish;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class SetmealDTO implements Serializable {
    // 【核心修复】统一为 Long
    private Long id;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private Integer status;
    private String description;
    private String image;
    private List<SetmealDish> setmealDishes = new ArrayList<>();
}
