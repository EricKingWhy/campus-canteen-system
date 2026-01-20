package fun.cyhgraph.controller.user;

import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.FavoriteService;
import fun.cyhgraph.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/favorite") // 注意路径
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/add")
    public Result<String> add(@RequestParam Long dishId) {
        favoriteService.add(dishId);
        return Result.success("收藏成功");
    }

    @PostMapping("/remove")
    public Result<String> remove(@RequestParam Long dishId) {
        favoriteService.remove(dishId);
        return Result.success("取消收藏");
    }

    @GetMapping("/list")
    public Result<List<DishVO>> list() {
        // 【核心修改】调用新名字的方法
        List<DishVO> list = favoriteService.listUserFavorites();
        return Result.success(list);
    }

    @GetMapping("/check/{dishId}")
    public Result<Boolean> check(@PathVariable Long dishId) {
        return Result.success(favoriteService.check(dishId));
    }
}
