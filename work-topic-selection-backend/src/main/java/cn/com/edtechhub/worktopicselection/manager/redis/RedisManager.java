package cn.com.edtechhub.worktopicselection.manager.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 管理类
 */
@Component
public class RedisManager {

    private static final DefaultRedisScript<Long> CONSUME_VALUE_SCRIPT = new DefaultRedisScript<>(
            "local value = redis.call('GET', KEYS[1]); " +
                    "if not value then return 0; end; " +
                    "redis.call('DEL', KEYS[1]); " +
                    "if value == ARGV[1] then return 1; end; " +
                    "return -1;",
            Long.class
    );

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local count = redis.call('INCR', KEYS[1]); " +
                    "if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; " +
                    "return count;",
            Long.class
    );

    private static final DefaultRedisScript<Long> RELEASE_RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local value = redis.call('GET', KEYS[1]); " +
                    "if not value then return 0; end; " +
                    "local count = tonumber(value); " +
                    "if count <= 1 then redis.call('DEL', KEYS[1]); return 0; end; " +
                    "return redis.call('DECR', KEYS[1]);",
            Long.class
    );

    /**
     * 注入 RedisConfig 配置依赖
     */
    @Resource
    private RedisConfig redisConfig;

    /**
     * 注入 StringRedisTemplate 模板依赖
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 设置值, 带过期时间(秒)
     *
     * @param key            键
     * @param value          值
     * @param timeoutSeconds 过期时间
     */
    public void setValue(String key, String value, long timeoutSeconds) {
        stringRedisTemplate.opsForValue().set(redisConfig.getKeyPrefix() + key, value, timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * 设置值
     *
     * @param key   键
     * @param value 值
     */
    public void setValue(String key, String value) {
        stringRedisTemplate.opsForValue().set(redisConfig.getKeyPrefix() + key, value);
    }

    /**
     * 获取值
     *
     * @param key 键
     */
    public String getValue(String key) {
        return stringRedisTemplate.opsForValue().get(redisConfig.getKeyPrefix() + key);
    }

    /**
     * 删除键
     *
     * @param key 键
     */
    public void deleteKey(String key) {
        stringRedisTemplate.delete(redisConfig.getKeyPrefix() + key);
    }

    /**
     * 原子读取并删除一次性值。
     *
     * @return 1 表示匹配，0 表示不存在，-1 表示不匹配（不匹配时同样失效）
     */
    public long consumeValue(String key, String expectedValue) {
        Long result = stringRedisTemplate.execute(
                CONSUME_VALUE_SCRIPT,
                Collections.singletonList(redisConfig.getKeyPrefix() + key),
                expectedValue
        );
        return result == null ? 0L : result;
    }

    /**
     * 固定时间窗口限流。计数和首次过期时间设置由 Redis 原子完成。
     */
    public boolean tryAcquire(String key, int maxAttempts, long windowSeconds) {
        Long count = stringRedisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                Collections.singletonList(redisConfig.getKeyPrefix() + key),
                String.valueOf(windowSeconds)
        );
        return count != null && count <= maxAttempts;
    }

    /**
     * 归还一次已经占用的限流额度，保留原窗口的过期时间。
     */
    public void releaseRateLimit(String key) {
        stringRedisTemplate.execute(
                RELEASE_RATE_LIMIT_SCRIPT,
                Collections.singletonList(redisConfig.getKeyPrefix() + key)
        );
    }

    /**
     * 批量获取所有匹配的键
     *
     * @param pattern 匹配模式
     */
    public Set<String> getKeysByPattern(String pattern) {
        Set<String> keysWithPrefix = stringRedisTemplate.keys(redisConfig.getKeyPrefix() + pattern);
        if (keysWithPrefix == null) {
            return Collections.emptySet();
        }
        // 去掉前缀再返回
        return keysWithPrefix
                .stream()
                .map(key -> key.replaceFirst(redisConfig.getKeyPrefix(), ""))
                .collect(Collectors.toSet());
    }

    /**
     * 批量删除所有要求的键
     */
    public void deleteKeys(Collection<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            keys = keys
                    .stream()
                    .map(key -> redisConfig.getKeyPrefix() + key)
                    .collect(Collectors.toSet());
            stringRedisTemplate.delete(keys);
        }
    }

    /**
     * List 操作
     *
     * @param key   键
     * @param value 值
     */
    public void rightPushList(String key, String value) {
        stringRedisTemplate.opsForList().rightPush(redisConfig.getKeyPrefix() + key, value);
    }

    /**
     * List 操作
     *
     * @param key 键
     */
    public String leftPopList(String key) {
        return stringRedisTemplate.opsForList().leftPop(redisConfig.getKeyPrefix() + key);
    }

    /**
     * Set 操作
     *
     * @param key    键
     * @param values 值
     */
    public void addSet(String key, String... values) {
        stringRedisTemplate.opsForSet().add(redisConfig.getKeyPrefix() + key, values);
    }

    /**
     * Set 操作
     *
     * @param key 键
     */
    public Object getSetMembers(String key) {
        return stringRedisTemplate.opsForSet().members(redisConfig.getKeyPrefix() + key);
    }

    /**
     * Hash 操作
     *
     * @param key   键
     * @param field 字段
     * @param value 值
     */
    public void putHash(String key, String field, String value) {
        stringRedisTemplate.opsForHash().put(redisConfig.getKeyPrefix() + key, field, value);
    }

    /**
     * Hash 操作
     *
     * @param key   键
     * @param field 字段
     */
    public Object getHash(String key, String field) {
        return stringRedisTemplate.opsForHash().get(redisConfig.getKeyPrefix() + key, field);
    }

}
