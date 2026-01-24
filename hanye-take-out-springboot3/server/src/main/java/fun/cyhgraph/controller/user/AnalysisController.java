package fun.cyhgraph.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.entity.*;
import fun.cyhgraph.mapper.*;
import fun.cyhgraph.result.Result;
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
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    // ========== 健康分析接口 ==========

    /**
     * 获取健康分析摘要（今日摄入 + 营养结构 + BMI）
     */
    @GetMapping("/health/summary")
    public Result<Map<String, Object>> getHealthSummary() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取健康分析摘要，用户ID: {}", userId);

        User user = userMapper.selectById(userId);
        Map<String, Object> result = new HashMap<>();

        // 检查画像是否完整
        boolean hasProfile = user != null &&
                user.getGender() != null &&
                user.getAge() != null &&
                user.getHeight() != null &&
                user.getWeight() != null;
        result.put("hasProfile", hasProfile);

        if (!hasProfile || user == null) {
            result.put("hasOrderData", false);
            return Result.success(result);
        }

        // BMI、TDEE、目标热量
        Double bmi = user.getBmi();
        Integer tdee = user.getTdee();
        Integer healthGoal = user.getHealthGoal();

        int goalKcal = tdee != null ? tdee : 2200;
        if (healthGoal != null) {
            switch (healthGoal) {
                case 1:
                    goalKcal = (int) (goalKcal * 0.8);
                    break; // 减脂 -20%
                case 2:
                    goalKcal = (int) (goalKcal * 1.15);
                    break; // 增肌 +15%
            }
        }

        result.put("bmi", bmi != null ? Math.round(bmi * 10) / 10.0 : null);
        result.put("tdee", tdee);
        result.put("goalKcal", goalKcal);

        // 活动量标签
        Double activityFactor = user.getActivityFactor();
        String activityLabel = "中等";
        if (activityFactor != null) {
            if (activityFactor <= 1.25)
                activityLabel = "久坐";
            else if (activityFactor <= 1.45)
                activityLabel = "轻度";
            else if (activityFactor <= 1.65)
                activityLabel = "中等";
            else
                activityLabel = "重度";
        }
        result.put("activityLevelLabel", activityLabel);

        // 今日摄入热量和营养结构（从订单明细 + 菜品聚合）
        LocalDate today = LocalDate.now();
        NutritionData todayNutrition = calculateNutritionForDate(userId, today);

        result.put("todayIntakeKcal", todayNutrition.calories);
        result.put("hasOrderData", todayNutrition.hasData);

        // 营养结构
        if (todayNutrition.hasNutritionData) {
            Map<String, Object> macros = new HashMap<>();
            macros.put("proteinG", Math.round(todayNutrition.protein));
            macros.put("carbG", Math.round(todayNutrition.carbs));
            macros.put("fatG", Math.round(todayNutrition.fat));

            double total = todayNutrition.protein * 4 + todayNutrition.carbs * 4 + todayNutrition.fat * 9;
            if (total > 0) {
                macros.put("proteinPct", Math.round(todayNutrition.protein * 4 / total * 100));
                macros.put("carbPct", Math.round(todayNutrition.carbs * 4 / total * 100));
                macros.put("fatPct", Math.round(todayNutrition.fat * 9 / total * 100));
            } else {
                macros.put("proteinPct", 33);
                macros.put("carbPct", 34);
                macros.put("fatPct", 33);
            }
            result.put("macros", macros);
        } else {
            result.put("macros", null);
        }

        // 营养建议
        String suggestion = generateNutritionSuggestion(todayNutrition, goalKcal);
        result.put("suggestion", suggestion);

        return Result.success(result);
    }

    /**
     * 获取健康趋势（近7天/近30天）
     */
    @GetMapping("/health/trend")
    public Result<List<Map<String, Object>>> getHealthTrend(@RequestParam(defaultValue = "7") int range) {
        Long userId = BaseContext.getCurrentId();
        log.info("获取健康趋势，用户ID: {}, 范围: {}天", userId, range);

        User user = userMapper.selectById(userId);
        List<Map<String, Object>> trend = new ArrayList<>();

        // 计算目标热量
        int goalKcal = 2200;
        if (user != null && user.getTdee() != null) {
            goalKcal = user.getTdee();
            if (user.getHealthGoal() != null) {
                switch (user.getHealthGoal()) {
                    case 1:
                        goalKcal = (int) (goalKcal * 0.8);
                        break;
                    case 2:
                        goalKcal = (int) (goalKcal * 1.15);
                        break;
                }
            }
        }

        LocalDate today = LocalDate.now();
        for (int i = range - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            NutritionData nutrition = calculateNutritionForDate(userId, date);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("day", getDayName(date.getDayOfWeek()));
            dayData.put("intakeKcal", nutrition.calories);
            dayData.put("targetKcal", goalKcal);
            dayData.put("completionRate", goalKcal > 0 ? Math.round(nutrition.calories * 100.0 / goalKcal) : 0);
            trend.add(dayData);
        }

        log.info("健康趋势返回 {} 条数据", trend.size());
        return Result.success(trend);
    }

    // ========== 餐费分析接口 ==========

    /**
     * 获取餐费分析摘要
     */
    @GetMapping("/cost/summary")
    public Result<Map<String, Object>> getCostSummary() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取餐费分析摘要，用户ID: {}", userId);

        Map<String, Object> result = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());

        // 本月消费
        BigDecimal monthSpent = sumOrderAmount(userId, monthStart.atStartOfDay(), today.atTime(LocalTime.MAX));
        result.put("monthSpent", monthSpent.doubleValue());
        result.put("hasOrder", monthSpent.compareTo(BigDecimal.ZERO) > 0);

        // 预测月末消费
        int dayOfMonth = today.getDayOfMonth();
        int totalDaysInMonth = monthEnd.getDayOfMonth();
        double predictedMonthTotal = dayOfMonth > 0
                ? monthSpent.doubleValue() / dayOfMonth * totalDaysInMonth
                : 0;
        result.put("predictedMonthTotal", Math.round(predictedMonthTotal));

        // 基准线（上月消费）
        LocalDate lastMonthStart = monthStart.minusMonths(1);
        LocalDate lastMonthEnd = lastMonthStart.with(TemporalAdjusters.lastDayOfMonth());
        BigDecimal lastMonthSpent = sumOrderAmount(userId, lastMonthStart.atStartOfDay(),
                lastMonthEnd.atTime(LocalTime.MAX));
        double baseline = lastMonthSpent.doubleValue();
        result.put("baseline", baseline);

        // 消费状态标签
        String statusTag = "消费平稳";
        if (baseline > 0) {
            double ratio = predictedMonthTotal / baseline;
            if (ratio > 1.2)
                statusTag = "消费偏高";
            else if (ratio < 0.8)
                statusTag = "消费偏低";
        }
        result.put("statusTag", statusTag);

        // 消费提示
        String tip = "";
        if ("消费偏高".equals(statusTag)) {
            tip = "本月消费增速较快，预计比上月多花 ¥" + Math.round(predictedMonthTotal - baseline);
        } else if ("消费偏低".equals(statusTag)) {
            tip = "本月消费较上月有所减少，保持良好消费习惯！";
        }
        result.put("tip", tip);

        return Result.success(result);
    }

    /**
     * 获取消费趋势（近7天/近30天每日消费）
     */
    @GetMapping("/cost/trend")
    public Result<List<Map<String, Object>>> getCostTrend(@RequestParam(defaultValue = "7") int range) {
        Long userId = BaseContext.getCurrentId();
        log.info("获取消费趋势，用户ID: {}, 范围: {}天", userId, range);

        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = range - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            BigDecimal dayAmount = sumOrderAmount(userId, date.atStartOfDay(), date.atTime(LocalTime.MAX));

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("day", getDayName(date.getDayOfWeek()));
            dayData.put("value", dayAmount.doubleValue());
            dayData.put("isToday", i == 0);
            trend.add(dayData);
        }

        log.info("消费趋势返回 {} 条数据", trend.size());
        return Result.success(trend);
    }

    /**
     * 获取消费构成（按菜品分类聚合）
     */
    @GetMapping("/cost/composition")
    public Result<Map<String, Object>> getCostComposition(@RequestParam(defaultValue = "month") String range) {
        Long userId = BaseContext.getCurrentId();
        log.info("获取消费构成，用户ID: {}, 范围: {}", userId, range);

        Map<String, Object> result = new HashMap<>();

        // 确定时间范围
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        if ("week".equals(range)) {
            startDate = today.minusDays(7);
        } else {
            startDate = today.withDayOfMonth(1);
        }

        // 获取时间范围内的订单
        List<Orders> orders = getOrdersByUserAndTimeRange(userId, startDate.atStartOfDay(),
                today.atTime(LocalTime.MAX));

        if (orders.isEmpty()) {
            result.put("byCategory", new ArrayList<>());
            result.put("topCategoryName", "--");
            return Result.success(result);
        }

        // 获取订单明细并按分类聚合金额
        List<Long> orderIds = orders.stream().map(Orders::getId).collect(Collectors.toList());
        Map<String, Double> categoryAmounts = new HashMap<>();
        double totalAmount = 0;

        for (Long orderId : orderIds) {
            List<OrderDetail> details = orderDetailMapper.selectList(
                    new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId));

            for (OrderDetail detail : details) {
                if (detail.getDishId() != null) {
                    Dish dish = dishMapper.selectById(detail.getDishId());
                    if (dish != null && dish.getCategoryId() != null) {
                        Category category = categoryMapper.selectById(dish.getCategoryId());
                        String categoryName = category != null ? category.getName() : "其他";
                        double amount = detail.getAmount() != null ? detail.getAmount().doubleValue() : 0;
                        categoryAmounts.merge(categoryName, amount, Double::sum);
                        totalAmount += amount;
                    } else {
                        double amount = detail.getAmount() != null ? detail.getAmount().doubleValue() : 0;
                        categoryAmounts.merge("其他", amount, Double::sum);
                        totalAmount += amount;
                    }
                } else if (detail.getSetmealId() != null) {
                    double amount = detail.getAmount() != null ? detail.getAmount().doubleValue() : 0;
                    categoryAmounts.merge("套餐", amount, Double::sum);
                    totalAmount += amount;
                }
            }
        }

        // 构建分类列表
        List<Map<String, Object>> byCategory = new ArrayList<>();
        String[] colors = { "#ef4444", "#f59e0b", "#13ec5b", "#3b82f6", "#8b5cf6", "#ec4899" };
        int colorIndex = 0;

        final double finalTotal = totalAmount;
        List<Map.Entry<String, Double>> sortedCategories = categoryAmounts.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toList());

        for (Map.Entry<String, Double> entry : sortedCategories) {
            Map<String, Object> catData = new HashMap<>();
            catData.put("name", entry.getKey());
            catData.put("amount", Math.round(entry.getValue() * 100) / 100.0);
            catData.put("percent", finalTotal > 0 ? Math.round(entry.getValue() / finalTotal * 100) : 0);
            catData.put("color", colors[colorIndex % colors.length]);
            byCategory.add(catData);
            colorIndex++;
        }

        result.put("byCategory", byCategory);
        result.put("topCategoryName", sortedCategories.isEmpty() ? "--" : sortedCategories.get(0).getKey());

        log.info("消费构成返回 {} 个分类", byCategory.size());
        return Result.success(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 计算指定日期的营养摄入
     */
    private NutritionData calculateNutritionForDate(Long userId, LocalDate date) {
        NutritionData data = new NutritionData();

        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.atTime(LocalTime.MAX);

        // 获取当天已支付订单
        List<Orders> orders = getOrdersByUserAndTimeRange(userId, startTime, endTime);
        if (orders.isEmpty()) {
            return data;
        }

        data.hasData = true;

        // 遍历订单明细，聚合营养数据
        for (Orders order : orders) {
            List<OrderDetail> details = orderDetailMapper.selectList(
                    new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, order.getId()));

            for (OrderDetail detail : details) {
                if (detail.getDishId() != null) {
                    Dish dish = dishMapper.selectById(detail.getDishId());
                    if (dish != null) {
                        int quantity = detail.getNumber() != null ? detail.getNumber() : 1;

                        if (dish.getCalories() != null && dish.getCalories() > 0) {
                            data.calories += dish.getCalories() * quantity;
                            data.hasNutritionData = true;
                        }
                        if (dish.getProtein() != null) {
                            data.protein += dish.getProtein() * quantity;
                        }
                        if (dish.getCarbohydrates() != null) {
                            data.carbs += dish.getCarbohydrates() * quantity;
                        }
                        if (dish.getFat() != null) {
                            data.fat += dish.getFat() * quantity;
                        }
                    }
                }
            }
        }

        return data;
    }

    /**
     * 获取用户指定时间范围内的已支付订单
     */
    private List<Orders> getOrdersByUserAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return orderMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getUserId, userId)
                        .eq(Orders::getPayStatus, Orders.PAID)
                        .ge(Orders::getOrderTime, startTime)
                        .le(Orders::getOrderTime, endTime));
    }

    /**
     * 汇总订单金额
     */
    private BigDecimal sumOrderAmount(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Orders> orders = getOrdersByUserAndTimeRange(userId, startTime, endTime);
        return orders.stream()
                .map(o -> o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 生成营养建议
     */
    private String generateNutritionSuggestion(NutritionData nutrition, int goalKcal) {
        if (!nutrition.hasData) {
            return "今日还没有用餐记录，去点一份健康美食吧！";
        }
        if (!nutrition.hasNutritionData) {
            return "今日已点餐，但暂无菜品营养数据。";
        }

        double proteinRatio = nutrition.protein * 4 / Math.max(nutrition.calories, 1);
        double carbRatio = nutrition.carbs * 4 / Math.max(nutrition.calories, 1);

        if (proteinRatio < 0.15) {
            return "今日蛋白质摄入偏低，建议尝试鸡胸肉、鱼肉等高蛋白菜品。";
        } else if (carbRatio > 0.6) {
            return "今日碳水摄入偏高，建议减少主食，增加蔬菜和蛋白质。";
        } else if (nutrition.calories < goalKcal * 0.5) {
            return "今日摄入热量不足，注意保证充足的能量摄入。";
        } else if (nutrition.calories > goalKcal) {
            return "今日热量已达标，注意控制后续进食。";
        }
        return "今日营养摄入均衡，继续保持！";
    }

    private String getDayName(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "周一";
            case TUESDAY:
                return "周二";
            case WEDNESDAY:
                return "周三";
            case THURSDAY:
                return "周四";
            case FRIDAY:
                return "周五";
            case SATURDAY:
                return "周六";
            case SUNDAY:
                return "周日";
            default:
                return "";
        }
    }

    /**
     * 营养数据内部类
     */
    private static class NutritionData {
        boolean hasData = false;
        boolean hasNutritionData = false;
        double calories = 0;
        double protein = 0;
        double carbs = 0;
        double fat = 0;
    }
}
