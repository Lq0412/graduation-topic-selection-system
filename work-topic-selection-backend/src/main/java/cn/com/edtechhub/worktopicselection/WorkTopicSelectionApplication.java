package cn.com.edtechhub.worktopicselection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 启动类
 * 如果使用 IDEA 需要在启动前配置 .env 后才能启动
 * 如果使用 jdk 运行环境需要书写对应的环境变量才能启动
 * 如果使用 Docker 容器需要书写对应的环境变量才能启动
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Slf4j
@SpringBootApplication()
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class WorkTopicSelectionApplication {

    public static void main(String[] args) {
        /*ApplicationContext ctx = */
        SpringApplication.run(WorkTopicSelectionApplication.class, args);
        log.info("http://127.0.0.1:8000/doc.html");
    }

}

