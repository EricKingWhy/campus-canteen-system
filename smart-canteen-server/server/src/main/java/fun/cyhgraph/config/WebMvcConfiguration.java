package fun.cyhgraph.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fun.cyhgraph.interceptor.JwtTokenAdminInterceptor;
import fun.cyhgraph.interceptor.JwtTokenUserInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {
        private static final String UPLOAD_STATIC_ROOT_RELATIVE_DIR = "smart-canteen-admin/src/assets/images";

        @Autowired
        private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

        @Autowired
        private JwtTokenUserInterceptor jwtTokenUserInterceptor;

        /**
         * 注册自定义拦截器
         */
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                log.info("开始注册自定义拦截器...");

                // 1. 管理端拦截器
                registry.addInterceptor(jwtTokenAdminInterceptor)
                                .addPathPatterns("/admin/**")
                                .excludePathPatterns("/admin/employee/login",
                                                "/admin/employee/register",
                                                "/doc.html",
                                                "/webjars/**",
                                                "/swagger-resources",
                                                "/v2/api-docs",
                                                "/v3/api-docs/**",
                                                "/favicon.ico");

                // 2. 用户端拦截器
                registry.addInterceptor(jwtTokenUserInterceptor)
                                .addPathPatterns("/user/**", "/analysis/**")
                                .excludePathPatterns("/user/user/login",
                                                "/user/user/register",
                                                "/user/shop/status",
                                                "/doc.html",
                                                "/webjars/**",
                                                "/swagger-resources",
                                                "/v2/api-docs",
                                                "/v3/api-docs/**",
                                                "/favicon.ico")
                                .excludePathPatterns("/user/category/**")
                                .excludePathPatterns("/user/dish/**");
        }

        /**
         * 【核心修复】扩展消息转换器 - 强化版
         * 1. JavaTimeModule: 支持 LocalDateTime 序列化 (修复菜品列表报错)
         * 2. Long -> String: 防止前端 JS 处理大数字精度丢失 (修复员工ID/订单ID问题)
         * 
         * 策略：优先修改现有转换器，若不存在则添加新转换器
         */
        @Override
        public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
                log.info("扩展消息转换器（强化版 - Long转String）...");

                // 1. 构建增强型 ObjectMapper (支持 Long转String, 支持 LocalDateTime)
                ObjectMapper objectMapper = new ObjectMapper();

                // 注册时间模块
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                // 【关键】注册 Long -> String 序列化模块 (彻底解决JS精度丢失)
                SimpleModule simpleModule = new SimpleModule();
                simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
                simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
                objectMapper.registerModule(simpleModule);
                log.info("已注册 Long->String 序列化器，防止JS精度丢失");

                // 2. 遍历并更新所有现有 Jackson 转换器
                boolean found = false;
                for (HttpMessageConverter<?> converter : converters) {
                        if (converter instanceof MappingJackson2HttpMessageConverter) {
                                MappingJackson2HttpMessageConverter jacksonConverter = (MappingJackson2HttpMessageConverter) converter;
                                jacksonConverter.setObjectMapper(objectMapper);
                                log.info("已更新现有 MappingJackson2HttpMessageConverter 的 ObjectMapper");
                                found = true;
                                // 继续遍历，可能有多个Jackson转换器需要更新
                        }
                }

                // 3. 【保底机制】如果没找到任何Jackson转换器，添加一个新的到末尾
                if (!found) {
                        log.warn("未找到现有Jackson转换器，创建新转换器...");
                        MappingJackson2HttpMessageConverter newConverter = new MappingJackson2HttpMessageConverter();
                        newConverter.setObjectMapper(objectMapper);
                        converters.add(newConverter);
                        log.info("已添加新的 MappingJackson2HttpMessageConverter (带Long->String序列化)");
                }
        }

        /**
         * 设置静态资源映射 - 支持 Knife4j/Swagger 文档
         */
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                log.info("配置 Knife4j 静态资源映射...");
                // Knife4j UI 主页面
                registry.addResourceHandler("/doc.html")
                                .addResourceLocations("classpath:/META-INF/resources/");
                // WebJars 资源 (CSS/JS)
                registry.addResourceHandler("/webjars/**")
                                .addResourceLocations("classpath:/META-INF/resources/webjars/");
                // Swagger UI 资源
                registry.addResourceHandler("/swagger-ui/**")
                                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
                // Favicon
                registry.addResourceHandler("/favicon.ico")
                                .addResourceLocations("classpath:/META-INF/resources/");

                Path uploadStaticRootDir = resolveUploadStaticRootDirectory();
                try {
                        Files.createDirectories(uploadStaticRootDir.resolve("employee_photos"));
                } catch (IOException e) {
                        throw new RuntimeException("创建员工照片目录失败: " + uploadStaticRootDir.resolve("employee_photos"), e);
                }
                registry.addResourceHandler("/static/upload/**")
                                .addResourceLocations(uploadStaticRootDir.toUri().toString());

                // 【核心修复】静态资源映射 (项目 classpath 静态资源)
                registry.addResourceHandler("/static/**")
                                .addResourceLocations("classpath:/static/");
        }

        private Path resolveUploadStaticRootDirectory() {
                Path userDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
                List<Path> candidates = new ArrayList<>();

                candidates.add(userDir.resolve(UPLOAD_STATIC_ROOT_RELATIVE_DIR));

                Path current = userDir;
                for (int i = 0; i < 6 && current != null; i++) {
                        candidates.add(current.resolve(UPLOAD_STATIC_ROOT_RELATIVE_DIR));
                        current = current.getParent();
                }

                for (Path candidate : candidates) {
                        Path normalized = candidate.normalize();
                        if (Files.exists(normalized.getParent())) {
                                log.info("员工照片目录候选命中: {}", normalized);
                                return normalized;
                        }
                }

                Path fallback = userDir.resolve(UPLOAD_STATIC_ROOT_RELATIVE_DIR).normalize();
                log.warn("员工照片目录未命中已存在路径，使用fallback: {}", fallback);
                return fallback;
        }
}
