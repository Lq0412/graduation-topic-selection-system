package cn.com.edtechhub.worktopicselection.manager.caffeine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Caffeine 配置类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Component
@Data
@Slf4j
public class CaffeineConfig {

    /**
     * 键位前缀
     */
    private String keyPrefix = "work-topic-selection:";
    
    /**
     * 初始大小
     */
    private Integer initialCapacity = 1024;

    /**
     * 最大缓存
     */
    private Long maximumSize = 10000L;

    /**
     * 过期时间
     */
    private Integer expireAfterWrite = 10;

    /**
     * 打印配置
     */
    @PostConstruct
    public void printConfig() {
        log.debug("[CaffeineConfig] 当前项目 Caffeine 初始大小为 {}", this.initialCapacity);
        log.debug("[CaffeineConfig] 当前项目 Caffeine 最大缓存为 {}", this.maximumSize);
        log.debug("[CaffeineConfig] 当前项目 Caffeine 过期时间为 {}", this.expireAfterWrite);
    }

}
