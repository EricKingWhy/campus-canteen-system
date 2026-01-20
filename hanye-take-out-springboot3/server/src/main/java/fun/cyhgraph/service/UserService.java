package fun.cyhgraph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.cyhgraph.dto.UserLoginDTO;
import fun.cyhgraph.dto.UserRegisterDTO;
import fun.cyhgraph.entity.User;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     * 
     * @param userRegisterDTO 注册信息
     * @return 注册成功的用户
     */
    User register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户登录
     * 
     * @param userLoginDTO 登录信息
     * @return 登录成功的用户
     */
    User login(UserLoginDTO userLoginDTO);
}
