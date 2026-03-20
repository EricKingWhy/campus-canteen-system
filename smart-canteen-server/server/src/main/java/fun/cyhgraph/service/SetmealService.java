package fun.cyhgraph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.cyhgraph.dto.SetmealDTO;
import fun.cyhgraph.dto.SetmealPageDTO;
import fun.cyhgraph.entity.Setmeal;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.vo.DishItemVO;
import fun.cyhgraph.vo.SetmealVO;
import fun.cyhgraph.vo.SetmealWithPicVO; // 引入新 VO
import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    // Admin
    void addSetmeal(SetmealDTO setmealDTO);

    PageResult getPageList(SetmealPageDTO setmealPageDTO);

    SetmealVO getSetmealById(Integer id);

    void deleteBatch(List<Integer> ids);

    void onOff(Integer status, Integer id);

    void update(SetmealDTO setmealDTO);

    // User
    List<Setmeal> getList(Integer categoryId);

    List<DishItemVO> getSetmealDishesById(Integer id);

    // 【核心修复】返回值改为 SetmealWithPicVO，满足 Controller 需求
    SetmealWithPicVO getSetmealWithPic(Integer id);
}
