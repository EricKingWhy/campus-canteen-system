package fun.cyhgraph.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class CartDTO implements Serializable {
    private Long dishId;
    private Long setmealId;
    private String dishFlavor;
}
