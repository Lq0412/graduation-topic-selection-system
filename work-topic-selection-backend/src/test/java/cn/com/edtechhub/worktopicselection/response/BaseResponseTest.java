package cn.com.edtechhub.worktopicselection.response;

import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseResponse 纯单元测试（无依赖，仅测试类本身逻辑）
 */
class BaseResponseTest {

    // === 辅助测试的内部类（模拟业务对象） ===
    @Getter
    static class User {
        private final String name;
        private final int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

    }

    // === 测试构造方法 ===
    // 场景：测试「成功响应体」构造方法（带数据）
    @Test
    void testSuccessResponseConstructor() {
        // 准备测试数据
        CodeBindMessageEnums successEnum = CodeBindMessageEnums.SUCCESS;
        String testData = "测试数据";

        // 创建响应体
        BaseResponse<String> response = new BaseResponse<>(successEnum, testData);

        // 断言字段赋值正确
        assertEquals(successEnum.getCode(), response.getCode(), "状态码应匹配枚举值");
        assertEquals(successEnum.getMessage() + ": " + testData, response.getMessage(), "提示信息应匹配枚举值");
        assertEquals(null, response.getData(), "数据字段应正确赋值");
    }

    // 场景：测试「错误响应体」构造方法（带自定义提示）
    @Test
    void testErrorResponseConstructor() {
        // 1. 准备测试数据
        CodeBindMessageEnums paramErrorEnum = CodeBindMessageEnums.PARAMS_ERROR;
        String customMessage = "用户名不能为空";

        // 2. 创建响应体
        BaseResponse<Void> response = new BaseResponse<>(paramErrorEnum, customMessage);

        // 3. 断言字段赋值正确
        assertEquals(paramErrorEnum.getCode(), response.getCode(), "状态码应匹配枚举值");
        assertEquals(paramErrorEnum.getMessage() + ": " + customMessage, response.getMessage(), "提示信息应拼接自定义内容");
        assertNull(response.getData(), "错误响应体数据字段应为null");
    }

    // 场景：测试「临时响应体」构造方法（测试用）
    @Test
    void testTempResponseConstructor() {
        // 1. 准备测试数据
        int testCode = 1000;
        String testMsg = "临时测试";
        Integer testData = 123;

        // 2. 创建响应体
        BaseResponse<Integer> response = new BaseResponse<>(testCode, testMsg, testData);

        // 3. 断言字段赋值正确
        assertEquals(testCode, response.getCode(), "自定义状态码应生效");
        assertEquals(testMsg, response.getMessage(), "自定义提示信息应生效");
        assertEquals(testData, response.getData(), "自定义数据应生效");
    }

    // 场景：测试泛型适配（不同数据类型）
    @Test
    void testGenericTypeAdaptation() {
        // 测试整数类型数据
        BaseResponse<Integer> intResponse = new BaseResponse<>(CodeBindMessageEnums.SUCCESS, 666);
        assertEquals(Integer.class, intResponse.getData().getClass(), "数据类型应为Integer");

        // 测试对象类型数据
        User testUser = new User("张三", 20);
        BaseResponse<User> userResponse = new BaseResponse<>(CodeBindMessageEnums.SUCCESS, testUser);
        assertEquals(User.class, userResponse.getData().getClass(), "数据类型应为User");
        assertEquals("张三", userResponse.getData().getName(), "对象属性应正确");
    }

    // 场景：测试空数据场景
    @Test
    void testNullDataScenario() {
        // 成功响应体传入 null 数据
        BaseResponse<String> nullDataResponse = new BaseResponse<>(CodeBindMessageEnums.SUCCESS, null);
        assertEquals(0, nullDataResponse.getCode());
        assertEquals("成功: null", nullDataResponse.getMessage());
        assertNull(nullDataResponse.getData());
    }

}
