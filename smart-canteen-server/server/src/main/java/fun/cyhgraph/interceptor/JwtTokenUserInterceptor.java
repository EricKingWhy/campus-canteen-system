package fun.cyhgraph.interceptor;

import fun.cyhgraph.constant.JwtClaimsConstant;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.properties.JwtProperties;
import fun.cyhgraph.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1、从请求头中获取令牌
        // 【核心修复】这里可能配置的 header 名字和前端传的不一致，或者为 null
        String token = request.getHeader(jwtProperties.getUserTokenName());

        // 2、校验令牌
        try {
            // 【核心修复】增加判空，防止 NullPointerException
            if (token == null || token.isEmpty()) {
                log.warn("用户 Token 为空，拦截请求");
                response.setStatus(401);
                return false;
            }

            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前员工id:{}", userId);
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception ex) {
            log.error("用户 Token 校验失败: {}", ex.getMessage());
            response.setStatus(401);
            return false;
        }
    }
}