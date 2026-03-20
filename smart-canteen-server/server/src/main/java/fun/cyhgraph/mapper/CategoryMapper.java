package fun.cyhgraph.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.cyhgraph.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    // 继承 BaseMapper 后，自动拥有 CRUD 能力
}
