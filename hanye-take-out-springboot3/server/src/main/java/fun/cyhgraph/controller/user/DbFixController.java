package fun.cyhgraph.controller.user;

import fun.cyhgraph.entity.Category;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.CategoryService;
import fun.cyhgraph.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/user/db")
@Slf4j
public class DbFixController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;

    @PostMapping("/fix")
    public Result<String> fixData() {
        log.info("开始执行一键修复数据...");

        // 1. 修复分类 (热销推荐)
        long catId = 1001L;
        Category cat = categoryService.getById(catId);
        if (cat == null) {
            cat = new Category();
            cat.setId(catId);
            cat.setType(1);
            cat.setName("热销推荐");
            cat.setSort(0);
            cat.setStatus(1);
            cat.setCreateTime(LocalDateTime.now());
            cat.setUpdateTime(LocalDateTime.now());
            cat.setCreateUser(1L);
            cat.setUpdateUser(1L);
            try {
                categoryService.save(mapDto(cat));
                log.info("已创建分类: 热销推荐");
            } catch (Exception e) {
                log.error("创建分类失败 (可能是已存在): {}", e.getMessage());
            }
        } else {
            cat.setStatus(1);
            categoryService.updateById(cat);
            log.info("已更新分类状态: 热销推荐");
        }

        // 2. 修复菜品 (招牌红烧肉)
        long dishId = 2001L;
        Dish dish = dishService.getById(dishId);
        if (dish == null) {
            dish = new Dish();
            dish.setId(dishId);
            dish.setName("招牌红烧肉");
            dish.setCategoryId(catId);
            dish.setPrice(new BigDecimal("38.00"));
            dish.setImage("https://replicate.delivery/pbxt/J1Yq5X8Xj5X8Xj5X8Xj5X8Xj5X8Xj5X8/out-0.png");
            dish.setDescription("肥而不腻，入口即化，食堂一绝");
            dish.setStatus(1);
            dish.setCreateTime(LocalDateTime.now());
            dish.setUpdateTime(LocalDateTime.now());
            dish.setCreateUser(1L);
            dish.setUpdateUser(1L);

            fun.cyhgraph.dto.DishDTO dto = new fun.cyhgraph.dto.DishDTO();
            org.springframework.beans.BeanUtils.copyProperties(dish, dto);
            dishService.addDishWithFlavor(dto);
            log.info("已创建菜品: 招牌红烧肉");
        } else {
            dish.setStatus(1);
            dish.setCategoryId(catId);
            dishService.updateDishWithFlavor(mapDishDto(dish));
            log.info("已更新菜品: 招牌红烧肉");
        }

        return Result.success("数据修复成功！请刷新页面");
    }

    private fun.cyhgraph.dto.CategoryDTO mapDto(Category c) {
        fun.cyhgraph.dto.CategoryDTO dto = new fun.cyhgraph.dto.CategoryDTO();
        org.springframework.beans.BeanUtils.copyProperties(c, dto);
        dto.setId(c.getId());
        return dto;
    }

    private fun.cyhgraph.dto.DishDTO mapDishDto(Dish d) {
        fun.cyhgraph.dto.DishDTO dto = new fun.cyhgraph.dto.DishDTO();
        org.springframework.beans.BeanUtils.copyProperties(d, dto);
        dto.setId(d.getId());
        return dto;
    }
}
