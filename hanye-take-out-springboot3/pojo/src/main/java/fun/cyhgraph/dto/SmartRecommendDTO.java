package fun.cyhgraph.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 智选6道菜 - 前端传参 DTO
 */
@Data
public class SmartRecommendDTO implements Serializable {

    /** 是否有健康画像(用于冷启动判断) */
    private Boolean hasProfile;

    /** 目标总热量 TDEE (kcal) */
    private Double tdee;

    /** 今日已摄入热量 (kcal) */
    private Double todayCalories;

    /** 今日已摄入蛋白质 (g) */
    private Double todayProtein;

    /** 健康目标: 1减脂 2增肌 3维持 */
    private Integer healthGoal;

    /** 忌口标签(逗号分隔, 如"海鲜,花生") */
    private String avoidTags;
}
