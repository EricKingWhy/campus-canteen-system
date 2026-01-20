package fun.cyhgraph.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import fun.cyhgraph.constant.StatusConstant;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.DishService;
import fun.cyhgraph.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    // 【核心修复】增加 {categoryId} 路径参数，匹配前端请求
    // 【核心修复】改为接收 Query Param (Dish dish)，自动映射 categoryId 和 status
    @GetMapping("/list")
    public Result<List<DishVO>> list(Dish dish) {
        log.info("C端-查询菜品 categoryId={}", dish.getCategoryId());

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 优先使用前端传的 status (通常是1)，如果没有传则默认 1
        Integer status = dish.getStatus() != null ? dish.getStatus() : StatusConstant.ENABLE;
        queryWrapper.eq(Dish::getStatus, status);

        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishVO> dishVOList = list.stream().map(d -> {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);
            return dishVO;
        }).collect(Collectors.toList());

        return Result.success(dishVOList);
    }
}
