package cn.com.edtechhub.worktopicselection.manager.sentine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Sentine 配置类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Component
@Data
@Slf4j
public class SentineConfig {

    /**
     * 系统总用户数(当前给出 2000)
     */
    private double u = 2000;

    /**
     * 高峰同时在线比例, 0~1, 比如 0.05 表示 5% 同时请求
     */
    private double p = 0.75;

    /**
     * 每个在线用户每秒平均请求某个接口的次数, 接口特性
     */
    private double r = 2;

    /**
     * 安全系数, 防止突发流量, 通常 1~2(当前给出 1)
     */
    private double safety = 1;

    /**
     * qps
     */
    private Double qps = u * p * r * safety;

    /**
     * 打印配置
     */
    @PostConstruct
    public void printConfig() {
        log.debug("[{}] u: {}", this.getClass().getSimpleName(), this.u);
        log.debug("[{}] p: {}", this.getClass().getSimpleName(), this.p);
        log.debug("[{}] r: {}", this.getClass().getSimpleName(), this.r);
        log.debug("[{}] safety: {}", this.getClass().getSimpleName(), this.safety);
        log.debug("[{}] qps: {}", this.getClass().getSimpleName(), this.qps);
    }
    
}
