package fun.cyhgraph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.cyhgraph.dto.DishDTO;
import fun.cyhgraph.dto.DishPageDTO;
import fun.cyhgraph.dto.SmartRecommendDTO;
import fun.cyhgraph.entity.Dish;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.vo.SmartRecommendVO;
import java.util.List;

public interface DishService extends IService<Dish> {
    void addDishWithFlavor(DishDTO dishDTO);

    DishDTO getByIdWithFlavor(Integer id);

    List<Dish> getRecommendation(Integer id);

    void updateDishWithFlavor(DishDTO dishDTO);

    // 【核心修复】分页查询
    PageResult pageQuery(DishPageDTO dishPageDTO);

    // 【核心修复】启停菜品
    void startOrStop(Integer status, Long id);

    // 【核心修复】批量删除
    void deleteBatch(List<Long> ids);

    // 【核心修复】根据分类ID查询
    List<Dish> listByCategoryId(Long categoryId);

    void normalizeImageUrls(List<Dish> dishes);

    // 【临时工具】修复图片
    void fixImages();

    // 【智选6道菜】4层漏斗推荐引擎
    SmartRecommendVO getSmartPick6(SmartRecommendDTO dto);
}
