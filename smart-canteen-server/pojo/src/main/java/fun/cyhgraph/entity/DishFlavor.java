package fun.cyhgraph.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long dishId; // 菜品id
    private String name; // 口味名称
    @JsonAlias({ "list", "value" })
    @JsonProperty("list")
    @TableField("list")
    private String value; // 口味数据list
}
