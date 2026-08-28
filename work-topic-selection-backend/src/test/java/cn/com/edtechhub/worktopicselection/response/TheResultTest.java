package cn.com.edtechhub.worktopicselection.response;

import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TheResult 工具类纯单元测试
 * 覆盖所有静态方法，验证响应体构造逻辑
 */
class TheResultTest {

    // 辅助测试类
    @Getter
    static class User {
        private final String name;
        private final int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

    }

    // 场景：测试 success 方法（成功响应体）
    @Test
    void testSuccess() {
        // 1. 准备测试数据
        CodeBindMessageEnums successEnum = CodeBindMessageEnums.SUCCESS;
        String testData = "测试成功数据";

        // 2. 调用 success 方法
        BaseResponse<String> response = TheResult.success(successEnum, testData);

        // 3. 断言响应体字段正确
        assertEquals(successEnum.getCode(), response.getCode(), "状态码应匹配枚举值");
        assertEquals(successEnum.getMessage(), response.getMessage(), "提示信息应等于枚举消息");
        assertEquals(testData, response.getData(), "数据字段应正确赋值");
    }

    // 场景：测试 success 方法（数据为null的成功响应）
    @Test
    void testSuccessWithNullData() {
        // 1. 准备测试数据
        CodeBindMessageEnums successEnum = CodeBindMessageEnums.SUCCESS;

        // 2. 调用 success 方法（传入null数据）
        BaseResponse<Void> response = TheResult.success(successEnum, null);

        // 3. 断言响应体字段正确
        assertEquals(successEnum.getCode(), response.getCode());
        assertEquals(successEnum.getMessage(), response.getMessage());
        assertNull(response.getData(), "数据字段应为null");
    }

    // 场景：测试 error 方法（失败响应体）
    @Test
    void testError() {
        // 1. 准备测试数据
        CodeBindMessageEnums errorEnum = CodeBindMessageEnums.PARAMS_ERROR;
        String customMsg = "用户名不能为空";

        // 2. 调用 error 方法
        BaseResponse<Void> response = TheResult.error(errorEnum, customMsg);

        // 3. 断言响应体字段正确
        assertEquals(errorEnum.getCode(), response.getCode(), "状态码应匹配枚举值");
        assertEquals(errorEnum.getMessage() + ": " + customMsg, response.getMessage(), "提示信息应拼接自定义内容");
        assertNull(response.getData(), "失败响应体数据字段应为null");
    }

    // 场景：测试 notyet 方法（无参版 - 开发中响应）
    @Test
    void testNotyetWithoutParam() {
        // 1. 调用无参 notyet 方法
        BaseResponse<Void> response = TheResult.notyet();

        // 2. 断言响应体字段正确
        assertEquals(-1, response.getCode(), "状态码应为-1");
        assertEquals("该接口尚在开发中", response.getMessage(), "提示信息应匹配");
        assertNull(response.getData(), "数据字段应为null");
    }

    // 场景：测试 notyet 方法（有参版 - 带自定义提示）
    @Test
    void testNotyetWithParam() {
        // 1. 准备测试数据
        String customTestMsg = "暂未对接第三方接口";

        // 2. 调用有参 notyet 方法
        BaseResponse<Void> response = TheResult.notyet(customTestMsg);

        // 3. 断言响应体字段正确
        assertEquals(-1, response.getCode(), "状态码应为-1");
        assertEquals("该接口尚在开发中: " + customTestMsg, response.getMessage(), "提示信息应拼接自定义内容");
        assertNull(response.getData(), "数据字段应为null");
    }

    // 场景：测试泛型适配（不同数据类型）
    @Test
    void testGenericAdaptation() {
        // 测试整数类型数据
        BaseResponse<Integer> intResponse = TheResult.success(CodeBindMessageEnums.SUCCESS, 666);
        assertEquals(Integer.class, intResponse.getData().getClass(), "数据类型应为Integer");

        // 测试自定义对象类型数据
        User testUser = new User("张三", 20);
        BaseResponse<User> userResponse = TheResult.success(CodeBindMessageEnums.SUCCESS, testUser);
        assertEquals(User.class, userResponse.getData().getClass(), "数据类型应为User");
        assertEquals("张三", userResponse.getData().getName());
    }

}
