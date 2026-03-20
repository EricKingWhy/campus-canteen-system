package fun.cyhgraph.service;

import fun.cyhgraph.dto.CartDTO; // 切换为 CartDTO
import fun.cyhgraph.entity.ShoppingCart;
import java.util.List;

public interface CartService {
    void add(CartDTO cartDTO);

    List<ShoppingCart> getList(); // 【核心修复】改名为 getList

    void clean();

    void sub(CartDTO cartDTO);
}
