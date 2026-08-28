package cn.com.edtechhub.worktopicselection.aop;

import cn.com.edtechhub.worktopicselection.annotation.CacheSearchOptimization;
import cn.com.edtechhub.worktopicselection.constant.RedisConstant;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.manager.caffeine.CaffeineManager;
import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.model.enums.UserRoleEnum;
import cn.com.edtechhub.worktopicselection.response.BaseResponse;
import cn.com.edtechhub.worktopicselection.response.TheResult;
import cn.com.edtechhub.worktopicselection.service.UserService;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.time.Duration;

/**
 * 查询接口远端缓存优化注解切面
 */
@Aspect
@Component
@Slf4j
public class CacheSearchOptimizationAOP {

    @Resource
    private CaffeineManager caffeineManager;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserService userService;

    @Around("@annotation(cacheSearchOptimization)")
    public Object around(ProceedingJoinPoint joinPoint, CacheSearchOptimization cacheSearchOptimization) throws Throwable {
        // 如果是教师则跳过缓存, 只对学生进行缓存
        User loginUser = userService.userGetCurrentLoginUser();
        if (loginUser.getUserRole() == UserRoleEnum.TEACHER.getCode()) {
            return joinPoint.proceed();
        }

        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();

        // 构造缓存 key
        String queryCondition = JSONUtil.toJsonStr(args[0]);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String redisKey = RedisConstant.SEARCH_KEY_PREFIX + hashKey;
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();

        // 获取 modelClass 用于构建 TypeReference<Page<T>>
        Class<?> modelClass = cacheSearchOptimization.modelClass();
        TypeReference<Page<?>> typeReference = TypeBuilder.buildPageTypeReference(modelClass);

        log.debug("[CacheSearchOptimization] Redis 键名为 {}, 而检测到需要缓存的结果类型为 {} 包裹 {}", redisKey, typeReference, modelClass);

        // 本地缓存尝试
        String localCachedValue = (String) caffeineManager.get(redisKey);
        if (localCachedValue != null) {
            log.debug("[CacheSearchOptimization] 本地缓存命中");
            Page<?> cachedPage = JSONUtil.toBean(localCachedValue, typeReference, false);
            return TheResult.success(CodeBindMessageEnums.SUCCESS, cachedPage);
        }

        // Redis 尝试
        String remoteCachedValue = valueOps.get(redisKey);
        if (remoteCachedValue != null) {
            log.debug("[CacheSearchOptimization] 远端缓存命中");
            Page<?> cachedPage = JSONUtil.toBean(remoteCachedValue, typeReference, false);
            caffeineManager.put(redisKey, remoteCachedValue);
            return TheResult.success(CodeBindMessageEnums.SUCCESS, cachedPage);
        }

        // 无缓存 -> 执行原方法
        Object result = joinPoint.proceed();

        // 回写缓存
        if (result instanceof BaseResponse) {
            BaseResponse<?> response = (BaseResponse<?>) result;
            Object data = response.getData();
            if (data instanceof Page<?>) {
                long ttlSeconds = cacheSearchOptimization.ttl();
                String toCache = JSONUtil.toJsonStr(data);
                valueOps.set(redisKey, toCache, Duration.ofSeconds(ttlSeconds));
                log.debug("[CacheSearchOptimization] 写入远端缓存：{} -> {}", redisKey, toCache);
            } else {
                log.warn("[CacheSearchOptimization] 返回值非分页类型，跳过缓存");
            }
        } else {
            log.warn("[CacheSearchOptimization] 返回结果不是 BaseResponse，无法解析分页数据");
        }

        return result;
    }
}
