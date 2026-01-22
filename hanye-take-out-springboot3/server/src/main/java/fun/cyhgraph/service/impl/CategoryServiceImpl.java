package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.CategoryDTO;
import fun.cyhgraph.dto.CategoryPageDTO;
import fun.cyhgraph.entity.Category;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.entity.Setmeal;
import fun.cyhgraph.mapper.CategoryMapper;
import fun.cyhgraph.mapper.DishMapper;
import fun.cyhgraph.mapper.SetmealMapper;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setStatus(0);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        // 此时实体类已改为 Long，BaseContext 也是 Long，匹配成功
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        baseMapper.insert(category);
    }

    public PageResult page(CategoryPageDTO categoryPageDTO) {
        Page<Category> page = new Page<>(categoryPageDTO.getPage(), categoryPageDTO.getPageSize());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (categoryPageDTO.getName() != null)
            wrapper.like(Category::getName, categoryPageDTO.getName());
        if (categoryPageDTO.getType() != null)
            wrapper.eq(Category::getType, categoryPageDTO.getType());
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        baseMapper.selectPage(page, wrapper);
        return new PageResult(page.getTotal(), page.getRecords());
    }

    public void deleteById(Long id) {
        // 【逻辑审计修复】检查该分类下是否关联了菜品，若有则抛出异常
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(Dish::getCategoryId, id);
        Long dishCount = dishMapper.selectCount(dishWrapper);
        if (dishCount > 0) {
            throw new RuntimeException("该分类下存在 " + dishCount + " 道菜品，无法删除");
        }

        // 【逻辑审计修复】检查该分类下是否关联了套餐，若有则抛出异常
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
        setmealWrapper.eq(Setmeal::getCategoryId, id);
        Long setmealCount = setmealMapper.selectCount(setmealWrapper);
        if (setmealCount > 0) {
            throw new RuntimeException("该分类下存在 " + setmealCount + " 个套餐，无法删除");
        }

        baseMapper.deleteById(id);
    }

    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        baseMapper.updateById(category);
    }

    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder().id(id).status(status).updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId()).build();
        baseMapper.updateById(category);
    }

    public List<Category> list(Integer type) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (type != null)
            wrapper.eq(Category::getType, type);
        // wrapper.eq(Category::getStatus, 1); // 【临时修复】注释掉状态过滤，防止因数据未启用导致列表为空
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        return baseMapper.selectList(wrapper);
    }
}
