package fun.cyhgraph.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.cyhgraph.entity.Setmeal;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {
    // 兼容 Service 的调用，添加缺失方法 (即便为空也行，MyBatis Plus 会处理 BaseMapper 方法)
    // deleteBatchIds 是 BaseMapper 自带的，不需要额外声明
}
