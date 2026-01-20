package fun.cyhgraph.controller.user;

import fun.cyhgraph.entity.Category;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/user/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list(Integer type) {
        log.info("C端-查询分类 type={}", type);
        // 使用 MyBatis Plus 默认方法，规避手写 SQL 错误
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}
