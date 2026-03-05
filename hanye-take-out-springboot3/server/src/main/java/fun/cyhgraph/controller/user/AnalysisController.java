package fun.cyhgraph.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.entity.*;
import fun.cyhgraph.mapper.*;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 健康与餐费分析聚合接口 - 完整实现
 * 真实聚合订单数据，支持营养结构、健康趋势、消费构成
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    // ========== 健康分析接口 ==========

    /**
     * 获取健康分析摘要（今日摄入 + 营养结构 + BMI）
     */
    @GetMapping("/health/summary")
    public Result<Map<String, Object>> getHealthSummary() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(analysisService.getHealthSummary(userId));
    }

    /**
     * 获取健康趋势（近7天/近30天）
     */
    @GetMapping("/health/trend")
    public Result<List<Map<String, Object>>> getHealthTrend(@RequestParam(defaultValue = "7") int range) {
        Long userId = BaseContext.getCurrentId();
        return Result.success(analysisService.getHealthTrend(userId, range));
    }

    // ========== 餐费分析接口 ==========

    /**
     * 获取餐费分析摘要
     */
    @GetMapping("/cost/summary")
    public Result<Map<String, Object>> getCostSummary() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(analysisService.getCostSummary(userId));
    }
}
