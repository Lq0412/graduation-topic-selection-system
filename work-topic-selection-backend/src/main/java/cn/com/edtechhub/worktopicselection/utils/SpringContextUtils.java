package cn.com.edtechhub.worktopicselection.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文获取工具
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    /**
     * 存储 Spring 上下文的变量
     */
    private static ApplicationContext applicationContext;

    /**
     * 重写设置应用上下问的方法，方便我们设置 Spring 上下文以操作 Bean
     */
    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }

    /**
     * 通过名称获取 Bean
     */
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    /**
     * 通过类型获取 Bean
     */
    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    /**
     * 通过名称和类型获取 Bean
     */
    public static <T> T getBean(String beanName, Class<T> beanClass) {
        return applicationContext.getBean(beanName, beanClass);
    }

}
