package fun.cyhgraph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.cyhgraph.dto.DishDTO;
import fun.cyhgraph.entity.Dish;
import java.util.List;

public interface DishService extends IService<Dish> {
    void addDishWithFlavor(DishDTO dishDTO);

    DishDTO getByIdWithFlavor(Integer id);

    List<Dish> getRecommendation(Integer id);

    // 【核心修复】补全 Controller 调用的修改方法
    void updateDishWithFlavor(DishDTO dishDTO);
}
