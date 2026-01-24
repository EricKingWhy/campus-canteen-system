package fun.cyhgraph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.cyhgraph.dto.UserLoginDTO;
import fun.cyhgraph.dto.UserProfileDTO;
import fun.cyhgraph.dto.UserRegisterDTO;
import fun.cyhgraph.entity.User;
import fun.cyhgraph.vo.UserProfileVO;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    User register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户登录
     */
    User login(UserLoginDTO userLoginDTO);

    /**
     * 获取当前用户画像
     * 
     * @param userId 用户ID
     * @return 用户画像VO (含计算结果)
     */
    UserProfileVO getUserProfile(Long userId);

    /**
     * 更新用户画像 (部分更新)
     * 
     * @param userId 用户ID
     * @param dto    用户画像DTO
     * @return 更新后的用户画像VO
     */
    UserProfileVO updateProfile(Long userId, UserProfileDTO dto);
}
