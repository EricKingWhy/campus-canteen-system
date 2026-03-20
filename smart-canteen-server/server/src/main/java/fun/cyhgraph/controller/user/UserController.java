package fun.cyhgraph.controller.user;

import fun.cyhgraph.constant.JwtClaimsConstant;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.UserLoginDTO;
import fun.cyhgraph.dto.UserProfileDTO;
import fun.cyhgraph.dto.UserRegisterDTO;
import fun.cyhgraph.entity.User;
import fun.cyhgraph.properties.JwtProperties;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.UserService;
import fun.cyhgraph.utils.JwtUtil;
import fun.cyhgraph.vo.UserLoginVO;
import fun.cyhgraph.vo.UserProfileVO;
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

                // 🐛 测试专用后门 (Test Backdoor)
                if ("123456".equals(userLoginDTO.getCode())) {
                        log.info("测试后门登录：使用固定用户 1005");
                        User user = new User();
                        user.setId(1005L);
                        user.setOpenid("test_openid_123456");

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

        /**
         * 用户注册
         */
        @PostMapping("/register")
        public Result<UserLoginVO> register(@RequestBody UserRegisterDTO userRegisterDTO) {
                log.info("用户注册请求：{}", userRegisterDTO.getUsername());

                User user = userService.register(userRegisterDTO);

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

        /**
         * 获取用户信息 (by ID)
         */
        @GetMapping("/{id}")
        public Result<User> getById(@PathVariable Long id) {
                log.info("获取用户信息: {}", id);
                User user = userService.getById(id);
                return Result.success(user);
        }

        // ====================== 用户画像接口 ======================

        /**
         * 获取当前登录用户画像
         * - 从 JWT/BaseContext 获取 userId，不接受前端传入（防越权）
         */
        @GetMapping("/profile")
        public Result<UserProfileVO> getProfile() {
                Long userId = BaseContext.getCurrentId();
                log.info("获取用户画像: userId={}", userId);

                if (userId == null) {
                        return Result.error("用户未登录");
                }

                UserProfileVO profile = userService.getUserProfile(userId);
                return Result.success(profile);
        }

        /**
         * 更新当前登录用户画像
         * - 支持部分更新：只更新 DTO 中非 null 的字段
         * - 后端二次计算 BMI/BMR/TDEE 并落库
         */
        @PutMapping("/profile")
        public Result<UserProfileVO> updateProfile(@RequestBody UserProfileDTO dto) {
                Long userId = BaseContext.getCurrentId();
                log.info("更新用户画像: userId={}, dto={}", userId, dto);

                if (userId == null) {
                        return Result.error("用户未登录");
                }

                try {
                        UserProfileVO profile = userService.updateProfile(userId, dto);
                        return Result.success(profile);
                } catch (RuntimeException e) {
                        log.error("更新用户画像失败: {}", e.getMessage());
                        return Result.error(e.getMessage());
                }
        }
}
