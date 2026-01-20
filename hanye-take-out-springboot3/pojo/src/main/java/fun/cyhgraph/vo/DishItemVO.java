package fun.cyhgraph.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class DishItemVO implements Serializable {
    private String name;
    private Integer copies;
    private String image;
    private String description;
}
