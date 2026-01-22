package fun.cyhgraph.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j/Swagger 接口文档配置
 * 访问地址: http://localhost:8081/doc.html
 */
@Configuration
@Slf4j
public class Knife4jConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        log.info("初始化 Knife4j 接口文档配置...");
        return new OpenAPI()
                .info(new Info()
                        .title("校园食堂智能推荐系统 API 文档")
                        .version("1.0.0")
                        .description("基于 Spring Boot 3 + Vue 3 + Uni-app 的全栈食堂管理系统")
                        .contact(new Contact()
                                .name("王浩宇")
                                .email("admin@example.com")));
    }
}
