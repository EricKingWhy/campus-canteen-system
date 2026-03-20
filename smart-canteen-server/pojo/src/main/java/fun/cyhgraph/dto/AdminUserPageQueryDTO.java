package fun.cyhgraph.dto;

import lombok.Data;

/**
 * 管理端用户分页查询 DTO
 */
@Data
public class AdminUserPageQueryDTO {
    private String keyword; // 昵称/手机号/ID 模糊搜索
    private Integer status; // 状态筛选: 1启用 0禁用
    private String bmiRange; // BMI 区间: underweight/normal/overweight/obese
    private Integer page;
    private Integer pageSize;
}
