package cn.com.edtechhub.worktopicselection.manager.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine 管理器
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Component
@Slf4j
public class CaffeineManager {

    /**
     * 注入 CaffeineConfig 配置依赖
     */
    @Resource
    private CaffeineConfig caffeineConfig;

    /**
     * 需要后续初始化的缓存对象
     */
    private Cache<String, Object> cache;

    /**
     * 构造 Caffeine 缓存器实例
     */
    @PostConstruct
    public void init() {
        this.cache = Caffeine
                .newBuilder()
                .initialCapacity(caffeineConfig.getInitialCapacity())
                .maximumSize(caffeineConfig.getMaximumSize())
                .expireAfterWrite(caffeineConfig.getExpireAfterWrite(), TimeUnit.SECONDS)
                .build();
    }

    /**
     * 插入键值对
     *
     * @param key   键
     * @param value 值
     */
    public void put(String key, Object value) {
        this.cache.put(caffeineConfig.getKeyPrefix() + key, value);
    }

    /**
     * 获得键值堆
     *
     * @param key 键
     */
    public Object get(String key) {
        return this.cache.getIfPresent(caffeineConfig.getKeyPrefix() + key);
    }

    /**
     * 删除键值对
     *
     * @param key 键
     */
    public void remove(String key) {
        this.cache.invalidate(caffeineConfig.getKeyPrefix() + key);
    }

    /**
     * 清理所有键值对
     */
    public void clearAll() {
        this.cache.invalidateAll();
    }

    /**
     * 查看所有键值对
     */
    public Map<String, Object> dumpCache() {
        return this.cache.asMap();
    }

}
