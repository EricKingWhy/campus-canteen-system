package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.cyhgraph.constant.MessageConstant;
import fun.cyhgraph.dto.UserLoginDTO;
import fun.cyhgraph.dto.UserRegisterDTO;
import fun.cyhgraph.entity.User;
import fun.cyhgraph.exception.LoginFailedException;
import fun.cyhgraph.mapper.UserMapper;
import fun.cyhgraph.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册
     */
    @Override
    public User register(UserRegisterDTO userRegisterDTO) {
        log.info("用户注册：{}", userRegisterDTO.getUsername());

        // 1. 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(userRegisterDTO.getUsername());
        if (existingUser != null) {
            throw new LoginFailedException("用户名已存在，请更换一个用户名");
        }

        // 2. 创建新用户 (密码明文存储 - 生产环境应加密!)
        User newUser = User.builder()
                .username(userRegisterDTO.getUsername())
                .password(userRegisterDTO.getPassword())
                .email(userRegisterDTO.getEmail())
                .createTime(LocalDateTime.now())
                .build();

        // 3. 插入数据库
        userMapper.insert(newUser);
        log.info("注册成功，用户ID: {}", newUser.getId());

        return newUser;
    }

    /**
     * 用户登录
     */
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO.getUsername());

        // 1. 根据用户名查询用户
        User user = userMapper.selectByUsername(userLoginDTO.getUsername());

        // 2. 用户不存在
        if (user == null) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 3. 密码比对 (明文比对 - 生产环境应使用加密比对!)
        if (!userLoginDTO.getPassword().equals(user.getPassword())) {
            throw new LoginFailedException(MessageConstant.PASSWORD_ERROR);
        }

        log.info("登录成功，用户ID: {}", user.getId());
        return user;
    }
}
