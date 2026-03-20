package fun.cyhgraph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.cyhgraph.dto.CategoryPageDTO;
import fun.cyhgraph.entity.Category;
import fun.cyhgraph.result.PageResult;
import java.util.List;

public interface CategoryService extends IService<Category> {
    void save(fun.cyhgraph.dto.CategoryDTO categoryDTO);

    PageResult page(CategoryPageDTO categoryPageDTO);

    void deleteById(Long id);

    void update(fun.cyhgraph.dto.CategoryDTO categoryDTO);

    void startOrStop(Integer status, Long id);

    List<Category> list(Integer type);
}
