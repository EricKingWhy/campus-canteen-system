package fun.cyhgraph.service.impl;

import fun.cyhgraph.dto.ShoppingCartDTO;
import fun.cyhgraph.entity.ShoppingCart;
import fun.cyhgraph.service.ShoppingCartService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    public void add(ShoppingCartDTO dto) {
    }

    public List<ShoppingCart> showShoppingCart() {
        return new ArrayList<>();
    }

    public void clean() {
    }

    public void sub(ShoppingCartDTO dto) {
    }
}
