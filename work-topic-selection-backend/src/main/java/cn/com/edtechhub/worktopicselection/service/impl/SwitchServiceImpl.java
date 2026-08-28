package cn.com.edtechhub.worktopicselection.service.impl;

import cn.com.edtechhub.worktopicselection.manager.redis.RedisManager;
import cn.com.edtechhub.worktopicselection.service.SwitchService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 开关服务实现
 */
@Service
public class SwitchServiceImpl implements SwitchService {

    /**
     * 注入 Caffeine 管理器
     */
    @Resource
    private RedisManager redisManager;

    /**
     * 所有读写操作共用一把锁, 保证管理员的设置会立刻影响所有学生是否允许跨选
     * TODO: 不过实际上这是没有必要的, Redis 本身就是线程安全的, 不需要使用锁, 不过先先写着吧(而且这个锁也只是本地锁, 集群会出现问题)
     */
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public boolean isEnabled(String key) {
        lock.lock();
        try {
            String val = redisManager.getValue(key);
            return "true".equalsIgnoreCase(val); // 字符串转 boolean
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setEnabled(String key, boolean enabled) {
        lock.lock();
        try {
            redisManager.setValue(key, Boolean.toString(enabled)); // boolean 转字符串
        } finally {
            lock.unlock();
        }
    }

}
