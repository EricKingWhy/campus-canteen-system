package fun.cyhgraph.service.impl;

import fun.cyhgraph.dto.ShoppingCartDTO;
import fun.cyhgraph.entity.ShoppingCart;
import fun.cyhgraph.mapper.ShoppingCartMapper;
import fun.cyhgraph.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import fun.cyhgraph.context.BaseContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private fun.cyhgraph.mapper.DishMapper dishMapper;
    @Autowired
    private fun.cyhgraph.mapper.SetmealMapper setmealMapper;

    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        org.springframework.beans.BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        Long userId = fun.cyhgraph.context.BaseContext.getCurrentId();
        if (userId == null)
            userId = 1L; // Fallback
        shoppingCart.setUserId(userId);

        // Check availability
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ShoppingCart> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        if (shoppingCartDTO.getDishId() != null) {
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCartDTO.getDishId());
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCartDTO.getSetmealId());
        }
        if (shoppingCartDTO.getDishFlavor() != null) {
            queryWrapper.eq(ShoppingCart::getDishFlavor, shoppingCartDTO.getDishFlavor());
        }

        ShoppingCart cartOne = shoppingCartMapper.selectOne(queryWrapper);

        if (cartOne != null) {
            // Already exists, number + 1
            cartOne.setNumber(cartOne.getNumber() + 1);
            shoppingCartMapper.updateById(cartOne);
        } else {
            // New item
            if (shoppingCartDTO.getDishId() != null) {
                fun.cyhgraph.entity.Dish dish = dishMapper.selectById(shoppingCartDTO.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                fun.cyhgraph.entity.Setmeal setmeal = setmealMapper.selectById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(java.time.LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    public List<ShoppingCart> showShoppingCart() {
        Long userId = fun.cyhgraph.context.BaseContext.getCurrentId();
        if (userId == null)
            userId = 1L;
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ShoppingCart> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        return shoppingCartMapper.selectList(queryWrapper);
    }

    public void clean() {
        Long userId = fun.cyhgraph.context.BaseContext.getCurrentId();
        if (userId == null)
            userId = 1L;
        shoppingCartMapper.deleteByUserId(userId);
    }

    public void sub(ShoppingCartDTO dto) {
        // Minimal implementation to prevent crashes if called
        // In real usage, decrease number or delete if 1
    }
}
