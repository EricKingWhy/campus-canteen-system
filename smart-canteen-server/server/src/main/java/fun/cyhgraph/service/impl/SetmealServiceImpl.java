package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.cyhgraph.dto.SetmealDTO;
import fun.cyhgraph.dto.SetmealPageDTO;
import fun.cyhgraph.entity.Setmeal;
import fun.cyhgraph.mapper.SetmealMapper;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.service.SetmealService;
import fun.cyhgraph.vo.DishItemVO;
import fun.cyhgraph.vo.SetmealVO;
import fun.cyhgraph.vo.SetmealWithPicVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;

    public void addSetmeal(SetmealDTO dto) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(dto, setmeal);
        setmealMapper.insert(setmeal);
    }

    public PageResult getPageList(SetmealPageDTO dto) {
        Page<Setmeal> page = new Page<>(dto.getPage(), dto.getPageSize());
        setmealMapper.selectPage(page, null);
        return new PageResult(page.getTotal(), page.getRecords());
    }

    public SetmealVO getSetmealById(Integer id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        SetmealVO vo = new SetmealVO();
        if (setmeal != null)
            BeanUtils.copyProperties(setmeal, vo);
        return vo;
    }

    public void deleteBatch(List<Integer> ids) {
        if (ids != null && !ids.isEmpty())
            setmealMapper.deleteBatchIds(ids);
    }

    public void onOff(Integer status, Integer id) {
        Setmeal s = new Setmeal();
        s.setId(Long.valueOf(id));
        s.setStatus(status);
        setmealMapper.updateById(s);
    }

    public void update(SetmealDTO dto) {
    }

    public List<Setmeal> getList(Integer categoryId) {
        return new ArrayList<>();
    }

    public List<DishItemVO> getSetmealDishesById(Integer id) {
        return new ArrayList<>();
    }

    // 【核心修复】返回 SetmealWithPicVO
    public SetmealWithPicVO getSetmealWithPic(Integer id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        SetmealWithPicVO vo = new SetmealWithPicVO();
        if (setmeal != null) {
            BeanUtils.copyProperties(setmeal, vo);
        }
        return vo;
    }
}
