package fun.cyhgraph.controller.user;

import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.FavoriteService;
import fun.cyhgraph.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/add")
    public Result<String> add(@RequestParam Long dishId) {
        Long userId = BaseContext.getCurrentId();
        log.info("【收藏】添加收藏 - userId: {}, dishId: {}", userId, dishId);
        favoriteService.add(dishId);
        return Result.success("收藏成功");
    }

    @PostMapping("/remove")
    public Result<String> remove(@RequestParam Long dishId) {
        Long userId = BaseContext.getCurrentId();
        log.info("【收藏】取消收藏 - userId: {}, dishId: {}", userId, dishId);
        favoriteService.remove(dishId);
        return Result.success("取消收藏");
    }

    @GetMapping("/list")
    public Result<List<DishVO>> list() {
        Long userId = BaseContext.getCurrentId();
        log.info("【收藏】获取收藏列表 - userId: {}", userId);
        List<DishVO> list = favoriteService.listUserFavorites();
        log.info("【收藏】返回收藏数量: {}", list.size());
        return Result.success(list);
    }

    @GetMapping("/check/{dishId}")
    public Result<Boolean> check(@PathVariable Long dishId) {
        Long userId = BaseContext.getCurrentId();
        Boolean isFavorite = favoriteService.check(dishId);
        log.info("【收藏】检查收藏状态 - userId: {}, dishId: {}, isFavorite: {}", userId, dishId, isFavorite);
        return Result.success(isFavorite);
    }
}
