package fun.cyhgraph.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户画像 VO - 返回给前端的健康画像 + 饮食偏好 + 计算结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    // ===== 基础信息 =====
    private Long id;
    private String nickname; // 昵称
    private String avatar; // 头像URL
    private String phone; // 手机号

    // ===== 健康画像 =====
    private Integer gender; // 性别: 1男 2女
    private Integer age; // 年龄
    private Double height; // 身高 cm
    private Double weight; // 体重 kg
    private Integer activityLevel; // 活动量: 1久坐 2轻度 3中度 4重度
    private Integer healthGoal; // 健康目标: 1减脂 2增肌 3维持

    // ===== 后端计算结果 =====
    private Double bmi; // BMI 体质指数 (保留1位小数)
    private String bmiCategory; // BMI 分类: 偏瘦/正常/超重/肥胖
    private Integer bmr; // BMR 基础代谢率
    private Integer tdee; // TDEE 总能量消耗
    private Integer suggestIntake; // 建议摄入 (根据健康目标调整)

    // ===== 饮食偏好 =====
    private List<String> tasteTags; // 口味偏好
    private List<String> avoidTags; // 忌口/过敏
    private String nutritionPref; // 营养偏好
    private BigDecimal mealBudget; // 每餐预算
}
