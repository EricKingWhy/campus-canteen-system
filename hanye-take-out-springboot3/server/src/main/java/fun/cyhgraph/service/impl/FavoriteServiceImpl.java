package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.entity.Favorite;
import fun.cyhgraph.mapper.DishMapper;
import fun.cyhgraph.mapper.FavoriteMapper;
import fun.cyhgraph.service.FavoriteService;
import fun.cyhgraph.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 获取当前用户ID (兼容处理)
     */
    private Long getCurrentUserId() {
        Object idObj = BaseContext.getCurrentId();
        if (idObj == null) {
            return 1L; // 兜底：如果没有登录，返回默认ID
        }
        return Long.valueOf(idObj.toString());
    }

    @Override
    public void add(Long dishId) {
        Long userId = getCurrentUserId();

        // 防止重复收藏
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId);
        queryWrapper.eq(Favorite::getDishId, dishId);
        Favorite one = favoriteMapper.selectOne(queryWrapper);

        if (one == null) {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setDishId(dishId);
            favorite.setCreateTime(LocalDateTime.now());
            favoriteMapper.insert(favorite);
        }
    }

    @Override
    public void remove(Long dishId) {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId);
        queryWrapper.eq(Favorite::getDishId, dishId);
        favoriteMapper.delete(queryWrapper);
    }

    @Override
    public List<DishVO> listUserFavorites() {
        Long userId = getCurrentUserId();

        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId);
        queryWrapper.orderByDesc(Favorite::getCreateTime);

        List<Favorite> favorites = favoriteMapper.selectList(queryWrapper);

        if (favorites == null || favorites.isEmpty()) {
            return new ArrayList<>();
        }

        // 提取 dishId 列表
        List<Long> dishIds = favorites.stream().map(Favorite::getDishId).collect(Collectors.toList());

        // 查出具体菜品信息
        if (dishIds.isEmpty())
            return new ArrayList<>(); // 防止空列表查询报错

        List<Dish> dishes = dishMapper.selectBatchIds(dishIds);

        if (dishes == null || dishes.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为 VO
        return dishes.stream().map(dish -> {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);
            return dishVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Boolean check(Long dishId) {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId);
        queryWrapper.eq(Favorite::getDishId, dishId);
        return favoriteMapper.selectCount(queryWrapper) > 0;
    }
}
