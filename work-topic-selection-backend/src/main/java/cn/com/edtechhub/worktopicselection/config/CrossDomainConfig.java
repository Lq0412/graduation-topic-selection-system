package cn.com.edtechhub.worktopicselection.config;

import cn.com.edtechhub.worktopicselection.aop.RequestLogAOP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * 全局跨域配置
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Configuration
@Slf4j
public class CrossDomainConfig implements WebMvcConfigurer {

    /**
     * 注入请求日志拦截切面依赖, 用于打印接口访问日志
     */
    @Resource
    private RequestLogAOP requestLogAOP;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * 配合切面拦截所有接口调用以提供详细的访问日志打印
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLogAOP).addPathPatterns("/**");
    }

    /**
     * 配置跨域共享
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**")
                .allowedOriginPatterns(this.getCorsRule().toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 允许跨域规则
     */
    private List<String> getCorsRule() {
        return java.util.Arrays.asList(StringUtils.commaDelimitedListToStringArray(allowedOrigins));
    }

    /**
     * 打印配置
     */
    @PostConstruct
    public void printConfig() {
        log.debug("[CrossDomainConfig] 当前项目 Cors 跨域规则为 {}", this.getCorsRule());
    }

}
