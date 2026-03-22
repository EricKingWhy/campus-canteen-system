package fun.cyhgraph.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import fun.cyhgraph.constant.StatusConstant;
import fun.cyhgraph.dto.DishDTO;
import fun.cyhgraph.dto.SmartRecommendDTO;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.DishService;
import fun.cyhgraph.vo.DishVO;
import fun.cyhgraph.vo.SmartRecommendVO;
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
        log.info("执行用户端菜品查询: name={}, categoryId={}", dish.getName(), dish.getCategoryId());

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        // 【核心修复】动态忽略分类 ID：如果传了 name，强制忽略 categoryId 以实现全库搜索！
        boolean hasName = dish.getName() != null && !dish.getName().trim().isEmpty();
        queryWrapper.eq(dish.getCategoryId() != null && !hasName, Dish::getCategoryId, dish.getCategoryId());

        // 【核心新增】支持通过 name 进行模糊搜索
        queryWrapper.like(hasName, Dish::getName, dish.getName());

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

    /**
     * C端-根据菜品ID获取详情(包含真实口味flavors)
     */
    @GetMapping("/dish/{id}")
    public Result<DishDTO> getDishDetail(@PathVariable Integer id) {
        log.info("C端-根据菜品ID查询详情(含口味): {}", id);
        DishDTO dishDTO = dishService.getByIdWithFlavor(id);
        return Result.success(dishDTO);
    }

    /**
     * 【核心新增】获取全校真实热销榜 (Top 10)
     */
    @GetMapping("/hotSales")
    public Result<List<DishVO>> hotSales() {
        log.info("C端-获取全校真实热销榜 Top 10");
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus, StatusConstant.ENABLE);
        queryWrapper.orderByDesc(Dish::getSold);
        queryWrapper.last("LIMIT 10");

        List<Dish> list = dishService.list(queryWrapper);
        List<DishVO> dishVOList = list.stream().map(d -> {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);
            return dishVO;
        }).collect(Collectors.toList());

        return Result.success(dishVOList);
    }

    /**
     * 【智选6道菜】4层漏斗推荐引擎接口
     */
    @PostMapping("/smartPick6")
    public Result<SmartRecommendVO> smartPick6(@RequestBody SmartRecommendDTO dto) {
        log.info("C端-智选6道菜, 入参: {}", dto);
        SmartRecommendVO result = dishService.getSmartPick6(dto);
        return Result.success(result);
    }
}
