package fun.cyhgraph.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.cyhgraph.dto.AdminUserPageQueryDTO;
import fun.cyhgraph.entity.*;
import fun.cyhgraph.mapper.*;
import fun.cyhgraph.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * 管理端 - 用户管理控制器
 */
@RestController
@RequestMapping("/admin/users")
@Slf4j
public class AdminUserController {

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
    @Autowired
    private fun.cyhgraph.service.AnalysisService analysisService;

    // ============ BMI 区间常量 ============
    private static final double BMI_UNDERWEIGHT = 18.5;
    private static final double BMI_NORMAL_MAX = 24.0;
    private static final double BMI_OVERWEIGHT_MAX = 28.0;

    /**
     * 用户分页列表 + 统计卡片
     */
    @GetMapping
    public Result<Map<String, Object>> page(AdminUserPageQueryDTO dto) {
        log.info("管理端用户分页查询: {}", dto);

        if (dto.getPage() == null)
            dto.setPage(1);
        if (dto.getPageSize() == null)
            dto.setPageSize(10);

        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty()) {
            String kw = dto.getKeyword().trim();
            wrapper.and(w -> w
                    .like(User::getNickname, kw)
                    .or().like(User::getName, kw)
                    .or().like(User::getPhone, kw)
                    .or().like(User::getId, kw));
        }

        // 状态筛选
        if (dto.getStatus() != null) {
            wrapper.eq(User::getStatus, dto.getStatus());
        }

        // BMI 区间筛选
        if (dto.getBmiRange() != null && !dto.getBmiRange().isEmpty()) {
            switch (dto.getBmiRange()) {
                case "underweight" -> wrapper.lt(User::getBmi, BMI_UNDERWEIGHT);
                case "normal" -> wrapper.ge(User::getBmi, BMI_UNDERWEIGHT).lt(User::getBmi, BMI_NORMAL_MAX);
                case "overweight" -> wrapper.ge(User::getBmi, BMI_NORMAL_MAX).lt(User::getBmi, BMI_OVERWEIGHT_MAX);
                case "obese" -> wrapper.ge(User::getBmi, BMI_OVERWEIGHT_MAX);
            }
        }

        wrapper.orderByDesc(User::getCreateTime);

        // 分页查询
        Page<User> page = new Page<>(dto.getPage(), dto.getPageSize());
        Page<User> userPage = userMapper.selectPage(page, wrapper);

        // 本月时间范围
        LocalDateTime monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime monthEnd = LocalDateTime.now();

        // 组装用户列表数据
        List<Map<String, Object>> userList = new ArrayList<>();
        for (User user : userPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", user.getId());
            item.put("nickname", user.getNickname() != null ? user.getNickname() : user.getName());
            item.put("avatar", user.getPic());
            item.put("gender", user.getGender());
            item.put("phone", user.getPhone());
            item.put("email", user.getEmail());
            item.put("bmi", user.getBmi());
            item.put("bmiLabel", getBmiLabel(user.getBmi()));
            item.put("tdee", user.getTdee());
            item.put("status", user.getStatus() != null ? user.getStatus() : 1);
            item.put("createTime", user.getCreateTime());

            // 本月消费
            BigDecimal monthSpend = getMonthSpendByUserId(user.getId(), monthStart, monthEnd);
            item.put("monthSpend", monthSpend);

            // 总订单数
            Long orderCount = orderMapper.selectCount(
                    new LambdaQueryWrapper<Orders>().eq(Orders::getUserId, user.getId()));
            item.put("orderCount", orderCount);

            userList.add(item);
        }

        // 统计卡片
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUsers", userMapper.selectCount(null));
        summary.put("activeUsers", getActiveUsersCount(monthStart, monthEnd));
        summary.put("totalSpend", getTotalMonthSpend(monthStart, monthEnd));

        // 组装返回
        Map<String, Object> result = new HashMap<>();
        result.put("list", userList);
        result.put("total", userPage.getTotal());
        result.put("summary", summary);

        return Result.success(result);
    }

    /**
     * 用户详情 - 档案与画像
     */
    @GetMapping("/{id}/profile")
    public Result<Map<String, Object>> getProfile(@PathVariable Long id) {
        log.info("获取用户档案: {}", id);

        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("nickname", user.getNickname() != null ? user.getNickname() : user.getName());
        profile.put("avatar", user.getPic());
        profile.put("phone", user.getPhone());
        profile.put("email", user.getEmail());
        profile.put("gender", user.getGender());
        profile.put("age", user.getAge());
        profile.put("height", user.getHeight());
        profile.put("weight", user.getWeight());
        profile.put("bmi", user.getBmi());
        profile.put("bmiLabel", getBmiLabel(user.getBmi()));
        profile.put("bmr", user.getBmr());
        profile.put("tdee", user.getTdee());
        profile.put("activityFactor", user.getActivityFactor());
        profile.put("healthGoal", user.getHealthGoal());
        profile.put("tasteTags", user.getTasteTags());
        profile.put("avoidTags", user.getAvoidTags());
        profile.put("nutritionPref", user.getNutritionPref());
        profile.put("mealBudget", user.getMealBudget());
        profile.put("status", user.getStatus() != null ? user.getStatus() : 1);
        profile.put("createTime", user.getCreateTime());

        // 判断档案是否完整
        boolean profileComplete = user.getHeight() != null && user.getWeight() != null
                && user.getHeight() > 0 && user.getWeight() > 0;
        profile.put("profileComplete", profileComplete);

        // 营养师建议
        profile.put("nutritionistTip", generateNutritionistTip(user));

        return Result.success(profile);
    }

    /**
     * 用户点餐记录
     */
    @GetMapping("/{id}/orders")
    public Result<Map<String, Object>> getOrders(
            @PathVariable Long id,
            @RequestParam(defaultValue = "7") Integer timeRange,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取用户点餐记录: userId={}, timeRange={}", id, timeRange);

        LocalDateTime startTime = LocalDateTime.now().minusDays(timeRange);

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, id)
                .ge(Orders::getOrderTime, startTime)
                .orderByDesc(Orders::getOrderTime);

        Page<Orders> orderPage = new Page<>(page, pageSize);
        Page<Orders> result = orderMapper.selectPage(orderPage, wrapper);

        // 组装订单数据（含明细）
        List<Map<String, Object>> orderList = new ArrayList<>();
        for (Orders order : result.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", order.getId());
            item.put("number", order.getNumber());
            item.put("orderTime", order.getOrderTime());
            item.put("amount", order.getAmount());
            item.put("status", order.getStatus());

            // 获取订单明细
            List<OrderDetail> details = orderDetailMapper.selectList(
                    new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, order.getId()));
            List<Map<String, Object>> detailList = new ArrayList<>();
            for (OrderDetail detail : details) {
                Map<String, Object> d = new HashMap<>();
                d.put("name", detail.getName());
                d.put("number", detail.getNumber());
                d.put("amount", detail.getAmount());

                // 获取菜品营养信息
                if (detail.getDishId() != null) {
                    Dish dish = dishMapper.selectById(detail.getDishId());
                    if (dish != null) {
                        d.put("calories", dish.getCalories());
                        d.put("protein", dish.getProtein());
                        d.put("fat", dish.getFat());
                        d.put("carbohydrates", dish.getCarbohydrates());
                    }
                }
                detailList.add(d);
            }
            item.put("details", detailList);
            orderList.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", orderList);
        data.put("total", result.getTotal());

        return Result.success(data);
    }

    /**
     * 餐费分析 (安全同源适配层)
     */
    @GetMapping("/{id}/analytics/spend")
    public Result<Map<String, Object>> getSpendAnalytics(
            @PathVariable Long id,
            @RequestParam(defaultValue = "7") Integer range) {
        log.info("获取用户餐费分析: userId={}, range={}", id, range);

        // 1. 获取同源底层服务聚合数据
        Map<String, Object> baseCost = analysisService.getCostSummary(id);

        // 2. 强类型安全适配前端所需的 key
        Map<String, Object> adminResponse = new HashMap<>();

        // 安全类型转换辅助方法内联，防止 ClassCastException
        adminResponse.put("monthSpend", safeToDouble(baseCost.get("monthSpent")));
        adminResponse.put("forecastMonthEnd", safeToDouble(baseCost.get("predictedMonthTotal")));

        adminResponse.put("statusLabel", baseCost.getOrDefault("statusTag", "暂无数据"));
        adminResponse.put("assistantTip", baseCost.getOrDefault("tip", "暂无更多消费建议"));

        adminResponse.put("spendComposition", baseCost.getOrDefault("byCategory", new ArrayList<>()));
        adminResponse.put("dailySpendTrend", baseCost.getOrDefault("trendData", new ArrayList<>()));

        return Result.success(adminResponse);
    }

    private Double safeToDouble(Object value) {
        if (value == null)
            return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 健康营养分析 - 适配层
     */
    @GetMapping("/{id}/analytics/nutrition")
    public Result<Map<String, Object>> getNutritionAnalytics(
            @PathVariable Long id,
            @RequestParam(defaultValue = "7") Integer range) {
        log.info("获取用户健康分析: userId={}, range={}", id, range);

        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> data = new HashMap<>();

        // 检查档案是否完整
        boolean profileComplete = user.getHeight() != null && user.getWeight() != null;
        data.put("profileComplete", profileComplete);

        if (!profileComplete) {
            data.put("emptyReason", "档案不完整，无法计算营养分析");
            return Result.success(data);
        }

        // 使用底层的 AnalysisService 同源能力
        Map<String, Object> summary = analysisService.getHealthSummary(id);
        List<Map<String, Object>> trends = analysisService.getHealthTrend(id, range);

        // 安全转换
        int targetCalories = Integer.parseInt(summary.getOrDefault("goalKcal", 2000).toString());
        data.put("targetCalories", targetCalories);

        // 整理趋势数据给 Admin 页面 （原 Admin 格式要求带有 actual 和 target 分数）
        List<Map<String, Object>> caloriesTrend = new ArrayList<>();
        int daysWithData = 0;

        for (Map<String, Object> t : trends) {
            // Trend 返回的数据是 intakeKcal, targetKcal
            Map<String, Object> point = new HashMap<>();
            point.put("date", t.get("date"));
            point.put("day", t.get("day"));
            double actual = Double.parseDouble(t.getOrDefault("intakeKcal", "0").toString());
            if (actual > 0)
                daysWithData++;

            point.put("actual", Math.round(actual));
            point.put("target", t.getOrDefault("targetKcal", targetCalories));
            point.put("isToday", t.get("date").equals(LocalDate.now().toString()));
            // Admin 端要求顺序从远到近 (倒序改顺序或一致)
            caloriesTrend.add(point);
        }

        // trend 从 AnalysisService出来可能是近几年到今天。原版要求逆向或正向均可，只需保持趋势。
        // Vue前端实际按序遍历，直接给过去即可
        data.put("caloriesTrend", caloriesTrend);

        // 提取今日数据 (最后一个元素即今日)
        if (!caloriesTrend.isEmpty()) {
            Map<String, Object> today = caloriesTrend.get(caloriesTrend.size() - 1);
            data.put("todayActual", today.get("actual"));
            data.put("todayTarget", today.get("target"));

            // 安全取值：不用强转 Integer
            long actual = Long.parseLong(today.get("actual").toString());
            long target = Long.parseLong(today.get("target").toString());
            data.put("todayRemaining", Math.max(0, target - actual));
            data.put("todayProgress", target > 0 ? Math.min(100, actual * 100 / target) : 0);
        }

        // 组装营养结构
        Map<String, Object> macrosData = (Map<String, Object>) summary.get("macros");
        Map<String, Object> macros = new HashMap<>();
        if (macrosData != null) {
            macros.put("protein", macrosData.getOrDefault("proteinG", 0));
            macros.put("carbs", macrosData.getOrDefault("carbG", 0));
            macros.put("fat", macrosData.getOrDefault("fatG", 0));
            macros.put("proteinPct", macrosData.getOrDefault("proteinPct", 0));
            macros.put("carbsPct", macrosData.getOrDefault("carbPct", 0));
            macros.put("fatPct", macrosData.getOrDefault("fatPct", 0));
        } else {
            macros.put("protein", 0);
            macros.put("carbs", 0);
            macros.put("fat", 0);
            macros.put("proteinPct", 0);
            macros.put("carbsPct", 0);
            macros.put("fatPct", 0);
        }
        data.put("macroBreakdown", macros);

        // 是否有数据
        data.put("hasData", daysWithData > 0);
        if (daysWithData == 0) {
            data.put("emptyReason", "暂无消费数据");
        }

        // BMI 数据
        data.put("bmi", user.getBmi());
        data.put("bmiLabel", getBmiLabel(user.getBmi()));

        return Result.success(data);
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/status/{status}")
    public Result<Void> updateStatus(@PathVariable Integer status, @RequestParam Long id) {
        log.info("更新用户状态: id={}, status={}", id, status);

        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);

        return Result.success();
    }

    /**
     * 导出用户数据 CSV
     */
    @GetMapping("/export")
    public void exportCsv(AdminUserPageQueryDTO dto, HttpServletResponse response) throws IOException {
        log.info("导出用户数据: {}", dto);

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=users_export.csv");

        // 查询所有符合条件的用户
        dto.setPage(1);
        dto.setPageSize(10000);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty()) {
            String kw = dto.getKeyword().trim();
            wrapper.and(w -> w
                    .like(User::getNickname, kw)
                    .or().like(User::getName, kw)
                    .or().like(User::getPhone, kw));
        }
        if (dto.getStatus() != null) {
            wrapper.eq(User::getStatus, dto.getStatus());
        }

        List<User> users = userMapper.selectList(wrapper);

        PrintWriter writer = response.getWriter();
        // BOM for Excel UTF-8
        writer.write('\ufeff');
        // Header
        writer.println("用户ID,昵称,手机号,性别,BMI,TDEE,状态,注册时间");

        for (User user : users) {
            writer.println(String.format("%d,%s,%s,%s,%.1f,%d,%s,%s",
                    user.getId(),
                    user.getNickname() != null ? user.getNickname() : user.getName(),
                    user.getPhone() != null ? user.getPhone() : "-",
                    user.getGender() != null ? (user.getGender() == 1 ? "男" : "女") : "-",
                    user.getBmi() != null ? user.getBmi() : 0,
                    user.getTdee() != null ? user.getTdee() : 0,
                    user.getStatus() != null && user.getStatus() == 1 ? "启用" : "禁用",
                    user.getCreateTime() != null ? user.getCreateTime().toLocalDate().toString() : "-"));
        }

        writer.flush();
    }

    // ============ 辅助方法 ============

    private String getBmiLabel(Double bmi) {
        if (bmi == null)
            return "未知";
        if (bmi < BMI_UNDERWEIGHT)
            return "偏瘦";
        if (bmi < BMI_NORMAL_MAX)
            return "正常";
        if (bmi < BMI_OVERWEIGHT_MAX)
            return "超重";
        return "肥胖";
    }

    private BigDecimal getMonthSpendByUserId(Long userId, LocalDateTime start, LocalDateTime end) {
        return orderMapper.sumAmountByUserIdAndTimeRange(userId, start, end, Orders.PAID);
    }

    private Long getActiveUsersCount(LocalDateTime start, LocalDateTime end) {
        List<Orders> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .ge(Orders::getOrderTime, start)
                        .le(Orders::getOrderTime, end)
                        .eq(Orders::getPayStatus, Orders.PAID));
        return orders.stream().map(Orders::getUserId).distinct().count();
    }

    private BigDecimal getTotalMonthSpend(LocalDateTime start, LocalDateTime end) {
        List<Orders> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .ge(Orders::getOrderTime, start)
                        .le(Orders::getOrderTime, end)
                        .eq(Orders::getPayStatus, Orders.PAID));
        return orders.stream()
                .map(o -> o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String getDayOfWeekCn(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "周一";
            case 2 -> "周二";
            case 3 -> "周三";
            case 4 -> "周四";
            case 5 -> "周五";
            case 6 -> "周六";
            case 7 -> "周日";
            default -> "";
        };
    }

    private List<Map<String, Object>> getSpendComposition(Long userId, LocalDateTime start, LocalDateTime end) {
        List<Orders> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getUserId, userId)
                        .ge(Orders::getOrderTime, start)
                        .le(Orders::getOrderTime, end)
                        .eq(Orders::getPayStatus, Orders.PAID));

        Map<Long, BigDecimal> categorySpend = new HashMap<>();
        for (Orders order : orders) {
            List<OrderDetail> details = orderDetailMapper.selectList(
                    new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, order.getId()));
            for (OrderDetail detail : details) {
                if (detail.getDishId() != null) {
                    Dish dish = dishMapper.selectById(detail.getDishId());
                    if (dish != null && dish.getCategoryId() != null) {
                        BigDecimal amount = detail.getAmount() != null ? detail.getAmount() : BigDecimal.ZERO;
                        categorySpend.merge(dish.getCategoryId(), amount, BigDecimal::add);
                    }
                }
            }
        }

        BigDecimal total = categorySpend.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        String[] colors = { "#3d9eff", "#60a5fa", "#93c5fd", "#bfdbfe", "#e2e8f0" };
        int colorIdx = 0;

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : categorySpend.entrySet()) {
            Category category = categoryMapper.selectById(entry.getKey());
            Map<String, Object> item = new HashMap<>();
            item.put("name", category != null ? category.getName() : "其他");
            item.put("amount", entry.getValue());
            item.put("percent", total.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().multiply(new BigDecimal(100)).divide(total, 0, BigDecimal.ROUND_HALF_UP)
                            .intValue()
                    : 0);
            item.put("color", colors[colorIdx % colors.length]);
            colorIdx++;
            result.add(item);
        }

        // 按金额排序
        result.sort((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")));

        return result;
    }

    private String generateNutritionistTip(User user) {
        if (user.getHealthGoal() == null || user.getTdee() == null) {
            return "请完善健康画像以获取个性化建议。";
        }
        int goal = user.getHealthGoal();
        int tdee = user.getTdee();

        return switch (goal) {
            case 1 -> String.format("减脂目标：建议每日控制在约%d~%dkcal，优先高蛋白低油脂菜品。",
                    tdee - 600, tdee - 400);
            case 2 -> String.format("增肌目标：建议每日摄入约%d~%dkcal，保证优质蛋白和复合碳水。",
                    tdee + 200, tdee + 400);
            default -> String.format("维持体重：建议每日摄入约%dkcal，注意营养均衡。", tdee);
        };
    }

    private String generateSpendTip(List<Map<String, Object>> trend, List<Map<String, Object>> composition) {
        // 简单分析逻辑
        if (trend.size() >= 2) {
            double recent = 0, earlier = 0;
            for (int i = 0; i < trend.size(); i++) {
                Object val = trend.get(i).get("value");
                double v = val instanceof BigDecimal ? ((BigDecimal) val).doubleValue() : 0;
                if (i >= trend.size() - 3)
                    recent += v;
                else
                    earlier += v;
            }
            if (recent > earlier * 1.5 && earlier > 0) {
                return "近几天消费有所上升，建议关注高价菜品选择。";
            }
        }
        if (!composition.isEmpty()) {
            String topCategory = (String) composition.get(0).get("name");
            return String.format("消费主要集中在%s类别，整体消费合理。", topCategory);
        }
        return "消费数据正常，继续保持良好习惯！";
    }
}
