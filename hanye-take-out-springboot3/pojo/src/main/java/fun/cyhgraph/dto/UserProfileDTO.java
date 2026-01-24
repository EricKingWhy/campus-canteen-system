package fun.cyhgraph.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户画像 DTO - 接收前端提交的健康画像 + 饮食偏好数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    // ===== 基础信息 =====
    private String nickname; // 昵称
    private String avatar; // 头像URL (映射到 pic 字段)
    private String phone; // 手机号

    // ===== 健康画像 =====
    private Integer gender; // 性别: 1男 2女
    private Integer age; // 年龄
    private Double height; // 身高 cm
    private Double weight; // 体重 kg
    private Integer activityLevel; // 活动量: 1久坐 2轻度 3中度 4重度 (接口层使用，后端转换为 activityFactor)
    private Integer healthGoal; // 健康目标: 1减脂 2增肌 3维持

    // ===== 饮食偏好 =====
    private List<String> tasteTags; // 口味偏好: ["辣","咸","甜"]
    private List<String> avoidTags; // 忌口/过敏: ["花生","麸质","乳制品"]
    private String nutritionPref; // 营养偏好: 高蛋白/低碳/生酮/均衡
    private BigDecimal mealBudget; // 每餐预算
}
