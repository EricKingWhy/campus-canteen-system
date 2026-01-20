package fun.cyhgraph.controller.admin;

import fun.cyhgraph.dto.CategoryDTO;
import fun.cyhgraph.dto.CategoryPageDTO;
import fun.cyhgraph.entity.Category;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController("adminCategoryController")
@RequestMapping("/admin/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public Result<String> save(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类：{}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageDTO categoryPageDTO) {
        log.info("分页查询：{}", categoryPageDTO);
        return Result.success(categoryService.page(categoryPageDTO));
    }

    @DeleteMapping
    public Result<String> deleteById(Long id) {
        log.info("删除分类：{}", id);
        categoryService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable("status") Integer status, Long id) {
        log.info("修改分类状态：{},{}", status, id);
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Category>> list(Integer type) {
        log.info("根据类型查询分类：{}", type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}
