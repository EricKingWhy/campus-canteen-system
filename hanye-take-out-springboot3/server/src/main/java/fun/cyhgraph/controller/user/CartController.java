package fun.cyhgraph.controller.user;

import fun.cyhgraph.dto.CartDTO;
import fun.cyhgraph.entity.ShoppingCart; // 明确引用 ShoppingCart
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public Result add(@RequestBody CartDTO cartDTO) {
        log.info("添加购物车:{}", cartDTO);
        cartService.add(cartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        // 【核心修复】返回 List<ShoppingCart>，不再使用 Cart
        return Result.success(cartService.getList());
    }

    @DeleteMapping("/clean")
    public Result clean() {
        cartService.clean();
        return Result.success();
    }

    @PostMapping("/sub")
    public Result sub(@RequestBody CartDTO cartDTO) {
        cartService.sub(cartDTO);
        return Result.success();
    }
}
