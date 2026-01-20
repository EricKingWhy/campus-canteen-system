package fun.cyhgraph.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.cyhgraph.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.io.Serializable;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    // 兼容性查询：使用 Integer
    @Select("select * from dish where id = #{id}")
    Dish getById(Serializable id);

    // 状态统计
    @Select("select count(id) from dish where status = #{status}")
    Integer getByStatus(Integer status);

    // 分类统计：参数改为 Integer
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Integer categoryId);

    // 【新增】增加销量
    void incrementSold(Long id);
}
