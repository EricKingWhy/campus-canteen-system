package fun.cyhgraph.service;

import java.util.List;
import java.util.Map;

public interface AnalysisService {

    /**
     * 获取用户健康分析摘要（今日摄入 + 营养结构 + BMI）
     *
     * @param userId 目标用户ID
     * @return 包含健康摘要指标的映射
     */
    Map<String, Object> getHealthSummary(Long userId);

    /**
     * 获取用户健康趋势（按天汇总的卡路里对比）
     *
     * @param userId 目标用户ID
     * @param range  查询范围（最近多少天）
     * @return 每天健康趋势的列表
     */
    List<Map<String, Object>> getHealthTrend(Long userId, int range);

    /**
     * 获取餐费分析摘要
     *
     * @param userId 目标用户ID
     * @return 包含餐费分析指标的映射
     */
    Map<String, Object> getCostSummary(Long userId);
}
