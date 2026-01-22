package fun.cyhgraph.controller.user;

import fun.cyhgraph.constant.JwtClaimsConstant;
import fun.cyhgraph.dto.UserLoginDTO;
import fun.cyhgraph.dto.UserRegisterDTO;
import fun.cyhgraph.entity.User;
import fun.cyhgraph.properties.JwtProperties;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.UserService;
import fun.cyhgraph.utils.JwtUtil;
import fun.cyhgraph.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController("userClientController")
@RequestMapping("/user/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 用户登录 (账号密码模式)
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录请求：{}", userLoginDTO.getUsername());

        // 1. 调用 Service 验证用户
        // 🐛 测试专用后门 (Test Backdoor)
        if ("123456".equals(userLoginDTO.getCode())) {
            log.info("测试后门登录：使用固定用户 1005");
            User user = new User();
            user.setId(1005L); // 之前的日志里你的用户ID是 1005
            user.setOpenid("test_openid_123456");

            // 生成 Token
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.USER_ID, user.getId());
            String token = JwtUtil.createJWT(
                    jwtProperties.getUserSecretKey(),
                    jwtProperties.getUserTtl(),
                    claims);

            UserLoginVO userLoginVO = UserLoginVO.builder()
                    .id(user.getId())
                    .openid(user.getOpenid())
                    .token(token)
                    .build();

            return Result.success(userLoginVO);
        }

        User user = userService.login(userLoginDTO);

        // 2. 生成 JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        // 3. 构建返回对象
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserLoginVO> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册请求：{}", userRegisterDTO.getUsername());

        // 1. 调用 Service 注册用户
        User user = userService.register(userRegisterDTO);

        // 2. 注册成功后自动登录，生成 JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        // 3. 构建返回对象
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        log.info("获取用户信息: {}", id);
        User user = userService.getById(id);
        return Result.success(user);
    }
}
