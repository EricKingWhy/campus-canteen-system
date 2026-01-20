package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.CartDTO; // 使用 CartDTO
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.entity.Setmeal;
import fun.cyhgraph.entity.ShoppingCart;
import fun.cyhgraph.mapper.DishMapper;
import fun.cyhgraph.mapper.SetmealMapper;
import fun.cyhgraph.mapper.ShoppingCartMapper;
import fun.cyhgraph.service.CartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    public void add(CartDTO cartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(cartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId() != null ? Long.valueOf(BaseContext.getCurrentId()) : 1L;
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.selectList(new LambdaQueryWrapper<>(shoppingCart));

        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateById(cart);
        } else {
            Long dishId = cartDTO.getDishId();
            if (dishId != null) {
                Dish dish = dishMapper.selectById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage()); // 现在 Dish 有 image 了
                shoppingCart.setAmount(dish.getPrice());
            } else {
                Long setmealId = cartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.selectById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    // 【核心修复】改名为 getList
    public List<ShoppingCart> getList() {
        Long userId = BaseContext.getCurrentId() != null ? Long.valueOf(BaseContext.getCurrentId()) : 1L;
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        return shoppingCartMapper.selectList(new LambdaQueryWrapper<>(shoppingCart));
    }

    public void clean() {
        Long userId = BaseContext.getCurrentId() != null ? Long.valueOf(BaseContext.getCurrentId()) : 1L;
        shoppingCartMapper.deleteByUserId(userId);
    }

    public void sub(CartDTO cartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(cartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId() != null ? Long.valueOf(BaseContext.getCurrentId()) : 1L;
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.selectList(new LambdaQueryWrapper<>(shoppingCart));
        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            Integer number = cart.getNumber();
            if (number == 1) {
                shoppingCartMapper.deleteById(cart.getId());
            } else {
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateById(cart);
            }
        }
    }
}
