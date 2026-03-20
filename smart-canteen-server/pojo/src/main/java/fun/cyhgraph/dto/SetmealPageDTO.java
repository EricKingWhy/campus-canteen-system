package fun.cyhgraph.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class SetmealPageDTO implements Serializable {
    private int page;
    private int pageSize;
    private String name;
    private Long categoryId;
    private Integer status;
}
