package fun.cyhgraph.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.cyhgraph.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);

    @Select("SELECT * FROM user WHERE openid = #{openid}")
    User selectByOpenid(String openid);
}
