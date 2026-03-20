package fun.cyhgraph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.cyhgraph.entity.Favorite;
import fun.cyhgraph.vo.DishVO;
import java.util.List;

public interface FavoriteService extends IService<Favorite> {
    void add(Long dishId);

    void remove(Long dishId);

    List<DishVO> listUserFavorites();

    Boolean check(Long dishId);
}
