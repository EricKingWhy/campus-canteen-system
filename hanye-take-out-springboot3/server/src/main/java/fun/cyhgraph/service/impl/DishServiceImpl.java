package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.cyhgraph.dto.DishDTO;
import fun.cyhgraph.dto.DishPageDTO;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.entity.DishFlavor;
import fun.cyhgraph.mapper.DishFlavorMapper;
import fun.cyhgraph.mapper.DishMapper;
import fun.cyhgraph.result.PageResult;
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
