package cn.com.edtechhub.worktopicselection.annotation;

import java.lang.annotation.*;

/**
 * 查询接口远端缓存优化注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheSearchOptimization {

    long ttl() default 30; // 默认缓存时间单位秒

    Class<?> modelClass(); // 新增，指定 Page<T> 中的 T 类型

}
