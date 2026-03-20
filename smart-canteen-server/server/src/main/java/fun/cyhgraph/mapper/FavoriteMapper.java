package fun.cyhgraph.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.cyhgraph.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
