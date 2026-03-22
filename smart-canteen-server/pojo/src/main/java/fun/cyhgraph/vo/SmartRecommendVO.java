package fun.cyhgraph.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 智选6道菜推荐结果
 */
@Data
@Builder
public class SmartRecommendVO implements Serializable {
    /**
     * 推荐模式：NORMAL | DIET | COLD_START
     */
    private String recommendMode;

    /**
     * 是否控卡模式（便于前端快速判断）
     */
    private Boolean isDietMode;

    /**
     * 推荐菜品列表
     */
    private List<DishVO> dishes;
}
