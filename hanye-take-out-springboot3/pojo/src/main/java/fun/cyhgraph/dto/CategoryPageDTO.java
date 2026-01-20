package fun.cyhgraph.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class CategoryPageDTO implements Serializable {
    // 【核心修复】创建这个缺失的类，解决 CategoryService 报错
    private int page;
    private int pageSize;
    private String name;
    private Integer type;
}
