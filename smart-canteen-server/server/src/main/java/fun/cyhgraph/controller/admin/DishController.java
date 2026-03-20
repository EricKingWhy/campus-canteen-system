package fun.cyhgraph.controller.admin;

import fun.cyhgraph.result.Result;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.dto.DishDTO;
import fun.cyhgraph.dto.DishPageDTO;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    // 新增
    @PostMapping
    public Result<String> save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.addDishWithFlavor(dishDTO);
        return Result.success("新增菜品成功");
    }

    /**
     * 【核心修复】分页查询菜品
     * 必须放在 /{id} 之前，否则 "page" 会被当作 id
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageDTO dishPageDTO) {
        log.info("分页查询菜品：{}", dishPageDTO);
        PageResult pageResult = dishService.pageQuery(dishPageDTO);
        return Result.success(pageResult);
    }

    // 根据ID查询
    @GetMapping("/{id}")
    public Result<DishDTO> get(@PathVariable Integer id) {
        log.info("根据ID查询菜品：{}", id);
        DishDTO dishDTO = dishService.getByIdWithFlavor(id);
        return Result.success(dishDTO);
    }

    // 修改
    @PutMapping
    public Result<String> update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.updateDishWithFlavor(dishDTO);
        return Result.success("修改菜品成功");
    }

    /**
     * 【核心修复】修改菜品状态 (起售/停售)
     */
    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("修改菜品状态：status={}, id={}", status, id);
        dishService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 【核心修复】批量删除菜品
     */
    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品：{}", ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    // 根据分类查询菜品列表
    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId) {
        log.info("根据分类查询菜品：{}", categoryId);
        List<Dish> list = dishService.listByCategoryId(categoryId);
        return Result.success(list);
    }

    /**
     * 【临时工具】修复图片路径
     * 请重启后端后访问: http://localhost:8081/admin/dish/fix-images
     */
    @GetMapping("/fix-images")
    public Result<String> fixImages() {
        dishService.fixImages();
        return Result.success("图片路径修复完成");
    }
}
