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

import java.util.List;

@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

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
                                                "/doc.html",
                                                "/webjars/**",
                                                "/swagger-resources",
                                                "/v2/api-docs",
                                                "/v3/api-docs/**",
                                                "/favicon.ico");

                // 2. 用户端拦截器
                registry.addInterceptor(jwtTokenUserInterceptor)
                                .addPathPatterns("/user/**")
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
         * 【核心修复】扩展消息转换器 - 温和修复模式
         * 1. JavaTimeModule: 支持 LocalDateTime 序列化 (修复菜品列表报错)
         * 2. Long -> String: 防止前端 JS 处理大数字精度丢失 (修复订单ID问题)
         * 
         * 关键改进：遍历现有转换器修改其ObjectMapper，而不是新增转换器到第0位
         * 这避免了破坏SpringDoc/Knife4j的ByteArray转换器顺序，解决了API文档返回Base64编码的问题
         */
        @Override
        public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
                log.info("扩展消息转换器（温和模式）...");

                // 1. 定义我们需要的高级 ObjectMapper (支持 Long转String, 支持 LocalDateTime)
                ObjectMapper objectMapper = new ObjectMapper();

                // 注册时间模块
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                // 注册 Long -> String 序列化模块 (解决前端精度丢失)
                SimpleModule simpleModule = new SimpleModule();
                simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
                simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
                objectMapper.registerModule(simpleModule);

                // 2. 关键修改：遍历现有转换器，只修改 Jackson 转换器，不破坏其他转换器(如 ByteArray)的顺序
                for (HttpMessageConverter<?> converter : converters) {
                        if (converter instanceof MappingJackson2HttpMessageConverter) {
                                MappingJackson2HttpMessageConverter jacksonConverter = (MappingJackson2HttpMessageConverter) converter;
                                jacksonConverter.setObjectMapper(objectMapper);
                                log.info("已更新现有 MappingJackson2HttpMessageConverter 的 ObjectMapper");
                                // 找到一个就够了，通常只有一个主要的 Jackson 转换器
                                break;
                        }
                }

                // 注意：我们不再执行 converters.add(0, converter); 这样就安全了！
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

                // 【核心修复】静态资源映射 (图片)
                registry.addResourceHandler("/static/**")
                                .addResourceLocations("classpath:/static/");
        }
}
