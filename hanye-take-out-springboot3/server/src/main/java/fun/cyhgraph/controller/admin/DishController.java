package fun.cyhgraph.controller.admin;

import fun.cyhgraph.result.Result; // 【核心修复】改回 Result
import fun.cyhgraph.dto.DishDTO;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    // 新增
    @PostMapping
    public Result<String> save(@RequestBody DishDTO dishDTO) {
        dishService.addDishWithFlavor(dishDTO);
        return Result.success("新增菜品成功");
    }

    // 根据ID查询 (参数统一为 Integer)
    @GetMapping("/{id}")
    public Result<DishDTO> get(@PathVariable Integer id) {
        DishDTO dishDTO = dishService.getByIdWithFlavor(id);
        return Result.success(dishDTO);
    }

    // 修改
    @PutMapping
    public Result<String> update(@RequestBody DishDTO dishDTO) {
        dishService.updateDishWithFlavor(dishDTO);
        return Result.success("修改菜品成功");
    }

    // 智能推荐接口
    @GetMapping("/list")
    public Result<List<Dish>> list(Dish dish) {
        List<Dish> list = dishService.getRecommendation(1);
        return Result.success(list);
    }
}
