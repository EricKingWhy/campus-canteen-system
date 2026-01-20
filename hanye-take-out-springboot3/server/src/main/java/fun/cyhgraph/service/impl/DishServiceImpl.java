package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.cyhgraph.dto.DishDTO;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.entity.DishFlavor;
import fun.cyhgraph.mapper.DishFlavorMapper;
import fun.cyhgraph.mapper.DishMapper;
import fun.cyhgraph.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

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

        // 现在 dishDTO.getId() 是 Long 类型，可以直接传入，不再报错
        saveFlavors(dishDTO, dishDTO.getId());
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
