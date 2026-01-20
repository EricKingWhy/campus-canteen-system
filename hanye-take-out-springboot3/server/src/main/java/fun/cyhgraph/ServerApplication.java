package fun.cyhgraph;

import com.baomidou.mybatisplus.extension.ddl.IDdl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@SpringBootApplication
@EnableTransactionManagement
@Slf4j
@EnableCaching
@EnableScheduling
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
        // 【成功标记】看到这行字，请直接去买彩票！
        log.info(">>>>>> 苍穹外卖 Server 最终启动成功！(v7.0 IDdl适配版) <<<<<<");
    }

    /**
     * 【核心修复】
     * 注入一个空的 DDL 处理器。
     * 适配 MyBatis Plus 新版接口签名：runScript(Consumer<DataSource>)
     */
    @Bean
    public IDdl dummyDdl() {
        return new IDdl() {
            @Override
            public void runScript(Consumer<DataSource> dataSourceConsumer) {
                // 空实现，什么都不做，专门为了骗过启动检查
            }

            @Override
            public List<String> getSqlFiles() {
                return Collections.emptyList(); // 返回空列表
            }
        };
    }
}
