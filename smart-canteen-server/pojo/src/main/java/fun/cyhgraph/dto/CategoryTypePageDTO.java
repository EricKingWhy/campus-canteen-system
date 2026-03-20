package fun.cyhgraph.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class CategoryTypePageDTO implements Serializable {
    private int page;
    private int pageSize;
    private String name;
    private Integer type; // 1 菜品分类 2 套餐分类
}
