package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.cyhgraph.dto.DishDTO;
import fun.cyhgraph.dto.DishPageDTO;
import fun.cyhgraph.dto.SmartRecommendDTO;
import fun.cyhgraph.entity.Category;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.entity.DishFlavor;
import fun.cyhgraph.mapper.CategoryMapper;
import fun.cyhgraph.mapper.DishFlavorMapper;
import fun.cyhgraph.mapper.DishMapper;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Transactional
    public void addDishWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        Long dishId = dish.getId();
        saveFlavors(dishDTO, dishId);
    }

    public DishDTO getByIdWithFlavor(Integer id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null)
            return null;
        DishDTO dto = new DishDTO();
        BeanUtils.copyProperties(dish, dto);

        // 查询口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        dto.setFlavors(dishFlavorMapper.selectList(queryWrapper));
        return dto;
    }

    public List<Dish> getRecommendation(Integer id) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus, 1);
        wrapper.last("limit 5");
        return dishMapper.selectList(wrapper);
    }

    @Transactional
    public void updateDishWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.updateById(dish);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDTO.getId());
        dishFlavorMapper.delete(queryWrapper);

        saveFlavors(dishDTO, dishDTO.getId());
    }

    /**
     * 【核心修复】分页查询菜品
     */
    @Override
    public PageResult pageQuery(DishPageDTO dishPageDTO) {
        Page<Dish> page = new Page<>(dishPageDTO.getPage(), dishPageDTO.getPageSize());
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();

        // 条件查询
        if (dishPageDTO.getName() != null && !dishPageDTO.getName().isEmpty()) {
            wrapper.like(Dish::getName, dishPageDTO.getName());
        }
        if (dishPageDTO.getCategoryId() != null) {
            wrapper.eq(Dish::getCategoryId, dishPageDTO.getCategoryId());
        }
        if (dishPageDTO.getStatus() != null) {
            wrapper.eq(Dish::getStatus, dishPageDTO.getStatus());
        }
        wrapper.orderByDesc(Dish::getUpdateTime);

        dishMapper.selectPage(page, wrapper);
        return new PageResult(page.getTotal(), page.getRecords());
    }

    /**
     * 【核心修复】启停菜品
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.updateById(dish);
    }

    /**
     * 【核心修复】批量删除菜品
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 删除菜品
        dishMapper.deleteBatchIds(ids);
        // 删除关联的口味
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DishFlavor::getDishId, ids);
        dishFlavorMapper.delete(wrapper);
    }

    /**
     * 【核心修复】根据分类ID查询菜品
     */
    @Override
    public List<Dish> listByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Dish::getCategoryId, categoryId);
        }
        wrapper.eq(Dish::getStatus, 1); // 只查起售的
        wrapper.orderByAsc(Dish::getSort);
        return dishMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void fixImages() {
        // 1. 宫保鸡丁
        updateImageByName("宫保鸡丁", "/static/dish/kung_pao_chicken.jpg");
        // 2. 可乐
        updateImageByName("可乐", "/static/dish/coca_cola.jpg");
        updateImageByName("可口可乐", "/static/dish/coca_cola.jpg");
        // 3. 红烧肉
        updateImageByName("红烧肉", "/static/dish/braised_pork.jpg");
        updateImageByName("东坡肉", "/static/dish/braised_pork.jpg");
    }

    // ============================================================
    // 【智选6道菜】4层漏斗推荐引擎
    // ============================================================
    @Override
    public List<Dish> getSmartPick6(SmartRecommendDTO dto) {
        log.info("===== 智选6道菜漏斗引擎启动 =====");
        log.info("入参: hasProfile={}, tdee={}, todayCalories={}, todayProtein={}, healthGoal={}, avoidTags={}",
                dto.getHasProfile(), dto.getTdee(), dto.getTodayCalories(),
                dto.getTodayProtein(), dto.getHealthGoal(), dto.getAvoidTags());

        // ────── 第1层：冷启动兜底 ──────
        if (dto.getHasProfile() == null || !dto.getHasProfile()) {
            log.info("[漏斗L1] 无画像 -> 冷启动: 按销量Top6");
            LambdaQueryWrapper<Dish> cold = new LambdaQueryWrapper<>();
            cold.eq(Dish::getStatus, 1);
            cold.orderByDesc(Dish::getSold);
            cold.last("LIMIT 6");
            return dishMapper.selectList(cold);
        }

        // ────── 第2层：时间与场景过滤 ──────
        LocalTime now = LocalTime.now();
        List<Long> sceneCategoryIds = getSceneCategoryIds(now);
        log.info("[漏斗L2] 当前时间={}, 场景分类IDs={}", now, sceneCategoryIds);

        // ────── 第3层：健康与忌口红线 ──────
        double gap = safeDouble(dto.getTdee()) - safeDouble(dto.getTodayCalories());
        log.info("[漏斗L3] 热量缺口={}kcal", gap);

        // ────── 第4层：动态排序策略 ──────
        // 先尝试完整过滤
        List<Dish> result = executeQuery(sceneCategoryIds, dto, gap, true, true);
        log.info("[漏斗结果] 完整过滤命中{}条", result.size());

        // ────── 安全容错：逐层放宽 ──────
        if (result.size() < 6) {
            log.info("[容错] 结果不足6条，放宽健康红线重试");
            result = executeQuery(sceneCategoryIds, dto, gap, false, true);
            log.info("[容错] 放宽红线后命中{}条", result.size());
        }
        if (result.size() < 6) {
            log.info("[容错] 仍不足6条，取消时间场景过滤");
            result = executeQuery(null, dto, gap, false, false);
            log.info("[容错] 全量查询命中{}条", result.size());
        }

        return result;
    }

    /**
     * 执行漏斗查询 - 【升级为双路召回+打分模型】
     * SQL粗筛获取Top20营养达标菜 + Java内存精筛打分（结合口味偏好）
     * 
     * @param categoryIds        场景分类ID列表(null=不限)
     * @param dto                前端传参
     * @param gap                热量缺口
     * @param applyHealthRedline 是否应用健康红线(卡路里/脂肪限制)
     * @param applyTimeFilter    是否应用时间场景过滤
     */
    private List<Dish> executeQuery(List<Long> categoryIds, SmartRecommendDTO dto,
            double gap, boolean applyHealthRedline, boolean applyTimeFilter) {
        LambdaQueryWrapper<Dish> w = new LambdaQueryWrapper<>();
        w.eq(Dish::getStatus, 1); // 基础条件：必须起售

        // L2: 时间场景
        if (applyTimeFilter && categoryIds != null && !categoryIds.isEmpty()) {
            w.in(Dish::getCategoryId, categoryIds);
        }

        // L3: 健康红线
        if (applyHealthRedline) {
            // 热量红线：菜品热量 ≤ 剩余缺口（仅当缺口>0且合理时）
            if (gap > 0 && gap < 5000) {
                w.isNotNull(Dish::getCalories);
                w.le(Dish::getCalories, gap);
            }
            // 减脂红线：限制高脂菜品
            if (dto.getHealthGoal() != null && dto.getHealthGoal() == 1) {
                w.and(wrapper -> wrapper
                        .isNull(Dish::getFat) // 没有脂肪数据的也保留
                        .or()
                        .lt(Dish::getFat, 15.0)); // 脂肪<15g
            }
        }

        // L3: 忌口红线（始终应用，这是安全底线）
        if (dto.getAvoidTags() != null && !dto.getAvoidTags().trim().isEmpty()) {
            String[] tags = dto.getAvoidTags().split(",");
            for (String tag : tags) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    w.and(wrapper -> wrapper
                            .isNull(Dish::getAllergenTags)
                            .or()
                            .notLike(Dish::getAllergenTags, trimmed));
                }
            }
        }

        // 【升级】SQL粗筛：先按销量降序捞出Top20候选菜品
        w.orderByDesc(Dish::getSold);
        w.last("LIMIT 20");
        List<Dish> candidateList = dishMapper.selectList(w);

        if (candidateList.isEmpty()) {
            return candidateList;
        }

        // 【核心升级】Java内存精筛打分排序
        List<String> userTastes = parseTastes(dto.getTasteTags());
        log.info("[智能打分] 用户口味偏好: {}, 候选菜品: {}道", userTastes, candidateList.size());

        List<Dish> finalPick = candidateList.stream()
                .sorted((d1, d2) -> {
                    int score1 = calculateScore(d1, userTastes, dto);
                    int score2 = calculateScore(d2, userTastes, dto);
                    return Integer.compare(score2, score1); // 降序：高分优先
                })
                .limit(6)
                .collect(Collectors.toList());

        // 打印打分透明日志
        for (Dish d : finalPick) {
            int score = calculateScore(d, userTastes, dto);
            log.info("[智能打分] 菜品: {} | 得分: {} | 热量: {}kcal | 蛋白: {}g",
                    d.getName(), score, d.getCalories(), d.getProtein());
        }
        log.info("[智能打分] 最终推荐 {} 道菜，已结合口味偏好 [{}] 进行提权打分",
                finalPick.size(), dto.getTasteTags());

        return finalPick;
    }

    /**
     * 【核心】菜品打分算法 - 千人千面个性化推荐
     * 基础分10 + 口味匹配(每命中+50) + 营养加分(蛋白质/低卡) + 销量加分
     */
    private int calculateScore(Dish dish, List<String> userTastes, SmartRecommendDTO dto) {
        int score = 10; // 基础分

        // 【权重最高】口味偏好匹配加分：每命中一个口味关键字 +50分
        String dishName = dish.getName() != null ? dish.getName() : "";
        String dishDesc = dish.getDescription() != null ? dish.getDescription() : "";
        String matchText = dishName + dishDesc;
        for (String taste : userTastes) {
            if (!taste.isEmpty() && matchText.contains(taste)) {
                score += 50;
            }
        }

        // 营养加分：蛋白质不足时，按蛋白质含量加分
        double todayProtein = safeDouble(dto.getTodayProtein());
        if (todayProtein < 60.0 && dish.getProtein() != null) {
            score += dish.getProtein().intValue();
        }

        // 营养加分：减脂目标时，低卡菜品加分
        if (dto.getHealthGoal() != null && dto.getHealthGoal() == 1 && dish.getCalories() != null) {
            if (dish.getCalories() < 300) {
                score += 20; // 低卡奖励
            }
        }

        // 增肌目标时，高蛋白菜品额外加分
        if (dto.getHealthGoal() != null && dto.getHealthGoal() == 2 && dish.getProtein() != null) {
            if (dish.getProtein() > 25) {
                score += 30; // 增肌高蛋白奖励
            }
        }

        // 销量加分（小幅度，防止冷门好菜被埋没）
        if (dish.getSold() != null) {
            score += Math.min(dish.getSold() / 10, 10); // 最多+10分
        }

        return score;
    }

    /**
     * 解析用户口味偏好标签
     * 支持逗号分隔字符串和JSON数组格式
     */
    private List<String> parseTastes(String tasteTags) {
        List<String> tastes = new ArrayList<>();
        if (tasteTags == null || tasteTags.trim().isEmpty()) {
            return tastes;
        }
        // 兼容 JSON 数组格式 ["辣","甜"] 和逗号分隔 "辣,甜"
        String cleaned = tasteTags.replaceAll("[\\[\\]\"']", "");
        for (String tag : cleaned.split(",")) {
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) {
                tastes.add(trimmed);
            }
        }
        return tastes;
    }

    /**
     * 根据时间段动态查找场景分类ID
     * 通过 category 表 name 模糊匹配，不硬编码ID
     */
    private List<Long> getSceneCategoryIds(LocalTime now) {
        List<String> keywords = new ArrayList<>();

        if (now.isAfter(LocalTime.of(6, 0)) && now.isBefore(LocalTime.of(10, 0))) {
            // 早餐时段
            keywords.add("早餐");
        } else if (now.isAfter(LocalTime.of(10, 0)) && now.isBefore(LocalTime.of(16, 30))) {
            // 午餐时段
            keywords.add("午餐");
            keywords.add("饮品");
            keywords.add("小吃");
        } else if (now.isAfter(LocalTime.of(16, 30)) && now.isBefore(LocalTime.of(21, 0))) {
            // 晚餐时段
            keywords.add("晚餐");
            keywords.add("饮品");
            keywords.add("小吃");
            keywords.add("轻食");
        } else {
            // 其他时段(深夜/凌晨) -> 不限制分类
            return Collections.emptyList();
        }

        // 动态查询分类ID
        LambdaQueryWrapper<Category> cw = new LambdaQueryWrapper<>();
        cw.eq(Category::getStatus, 1); // 只查启用的分类
        cw.and(wrapper -> {
            for (int i = 0; i < keywords.size(); i++) {
                if (i == 0) {
                    wrapper.like(Category::getName, keywords.get(i));
                } else {
                    wrapper.or().like(Category::getName, keywords.get(i));
                }
            }
        });

        List<Category> categories = categoryMapper.selectList(cw);
        List<Long> ids = categories.stream().map(Category::getId).collect(Collectors.toList());
        log.info("[分类匹配] 关键词={}, 匹配到分类={}", keywords,
                categories.stream().map(c -> c.getName() + "(#" + c.getId() + ")").collect(Collectors.joining(", ")));

        return ids;
    }

    /** null 安全的 double 提取 */
    private double safeDouble(Double val) {
        return val != null ? val : 0.0;
    }

    private void updateImageByName(String name, String image) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Dish::getName, name);
        Dish dish = new Dish();
        dish.setImage(image);
        dishMapper.update(dish, wrapper);
    }

    private void saveFlavors(DishDTO dishDTO, Long dishId) {
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> {
                flavor.setDishId(dishId);
                dishFlavorMapper.insert(flavor);
            });
        }
    }
}
