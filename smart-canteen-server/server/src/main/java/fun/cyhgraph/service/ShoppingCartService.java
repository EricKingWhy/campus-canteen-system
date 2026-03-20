package fun.cyhgraph.service;

import fun.cyhgraph.dto.ShoppingCartDTO;
import fun.cyhgraph.entity.ShoppingCart;
import java.util.List;

public interface ShoppingCartService {
    void add(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showShoppingCart();

    void clean();

    void sub(ShoppingCartDTO shoppingCartDTO);
}
