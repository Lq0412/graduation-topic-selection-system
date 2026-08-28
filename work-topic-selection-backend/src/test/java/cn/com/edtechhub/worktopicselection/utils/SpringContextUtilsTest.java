package cn.com.edtechhub.worktopicselection.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpringContextUtils 单元测试（结构化命名风格）
 */
@ExtendWith(SpringExtension.class)
// 加载测试专用的配置类（包含测试用 Bean + SpringContextUtils）
@ContextConfiguration(classes = SpringContextUtilsTest.TestConfig.class)
class SpringContextUtilsTest {

    // === 定义一个简单的类 ===
    static class TestService {
        public String sayHello() {
            return "TestService Hello";
        }
    }

    // === 定义测试专用配置类 ===
    @Configuration
    static class TestConfig {
        // 注册 SpringContextUtils 为 Bean（非常关键，让 Spring 自动注入上下文）
        @Bean
        public SpringContextUtils springContextUtils() {
            return new SpringContextUtils();
        }

        // 定义测试用的字符串类型 Bean
        @Bean(name = "testBean")
        public String testBean() {
            return "Hello SpringContextUtils";
        }

        // 定义测试用的对象类型 Bean
        @Bean
        public TestService testService() {
            return new TestService();
        }
    }

    // === 测试 getBean() ===
    /**
     * 场景：通过名称获取存在的 Bean → 返回对应 Bean 实例
     */
    @Test
    void getBean_givenExistBeanName_returnsBeanInstance() {
        // 执行方法：通过名称获取 testBean
        Object bean = SpringContextUtils.getBean("testBean");

        // 验证结果：Bean 不为 null，且值正确
        assertNotNull(bean);
        assertEquals("Hello SpringContextUtils", bean);
    }

    /**
     * 场景：通过类型获取存在的 Bean → 返回对应 Bean 实例
     */
    @Test
    void getBean_givenExistBeanClass_returnsBeanInstance() {
        // 执行方法：通过类型获取 TestService
        TestService testService = SpringContextUtils.getBean(TestService.class);

        // 验证结果：Bean 不为 null，且方法调用正常
        assertNotNull(testService);
        assertEquals("TestService Hello", testService.sayHello());
    }

    /**
     * 场景：通过名称 + 类型获取存在的 Bean → 返回对应 Bean 实例
     */
    @Test
    void getBean_givenExistBeanNameAndClass_returnsBeanInstance() {
        // 执行方法：通过名称 + 类型获取 testBean
        String testBean = SpringContextUtils.getBean("testBean", String.class);

        // 验证结果：Bean 不为 null，且值正确
        assertNotNull(testBean);
        assertEquals("Hello SpringContextUtils", testBean);
    }

    /**
     * 场景：通过名称获取不存在的 Bean → 抛出 BeansException
     */
    @Test
    void getBean_givenNotExistBeanName_throwsBeansException() {
        // 执行并断言异常
        assertThrows(BeansException.class, () -> {
            SpringContextUtils.getBean("notExistBean");
        });
    }

    /**
     * 场景：通过类型获取不存在的 Bean → 抛出 BeansException
     */
    @Test
    void getBean_givenNotExistBeanClass_throwsBeansException() {
        // 执行并断言异常（Integer 类型的 Bean 未定义）
        assertThrows(BeansException.class, () -> {
            SpringContextUtils.getBean(Integer.class);
        });
    }

    /**
     * 场景：通过名称 + 类型获取 Bean - 名称存在但类型不匹配 → 抛出 BeansException
     */
    @Test
    void getBean_givenExistBeanNameButMismatchClass_throwsBeansException() {
        // 执行并断言异常：testBean 是 String 类型，却用 TestService 类型获取
        assertThrows(BeansException.class, () -> {
            SpringContextUtils.getBean("testBean", TestService.class);
        });
    }

    /**
     * 场景：通过名称 + 类型获取 Bean - 类型存在但名称不匹配 → 抛出 BeansException
     */
    @Test
    void getBean_givenNotExistBeanNameAndAnyClass_throwsBeansException() {
        // 执行并断言异常：名称不存在，无论类型是否正确都抛异常
        assertThrows(BeansException.class, () -> {
            SpringContextUtils.getBean("notExistBean", String.class);
        });
    }

}
