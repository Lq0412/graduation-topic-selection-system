package cn.com.edtechhub.worktopicselection.manager.mybatisplus;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * MyBatis Plus 配置
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Configuration
@Slf4j
@MapperScan("cn.com.edtechhub.worktopicselection.mapper")
public class MyBatisPlusConfig {

    /**
     * 拦截器配置分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 分页插件
        return interceptor;
    }

    /**
     * 打印配置
     */
    @PostConstruct
    public void printConfig() {
        log.debug("[MyBatisPlusConfig] 当前项目为 MyBatis Plus 配置了分页插件");
    }

}
