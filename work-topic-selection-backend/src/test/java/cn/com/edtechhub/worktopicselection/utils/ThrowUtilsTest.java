package cn.com.edtechhub.worktopicselection.utils;

import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ThrowUtils 极简测试（只测核心逻辑，不测日志）
 */
class ThrowUtilsTest {

    private static final CodeBindMessageEnums TEST_ENUM = CodeBindMessageEnums.PARAMS_ERROR;
    private static final String TEST_MESSAGE = "参数错误";

    // === 测试 throwIf() ===
    /**
     * 场景：条件为 false → 不抛异常
     */
    @Test
    void throwIf_conditionFalse_doesNothing() {
        // 直接执行，不抛异常就通过
        assertDoesNotThrow(() -> ThrowUtils.throwIf(false, TEST_ENUM, TEST_MESSAGE));
    }

    /**
     * 场景：条件为 true → 抛出正确的 BusinessException
     */
    @Test
    void throwIf_conditionTrue_throwsBusinessException() {
        // 捕获异常并验证
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            ThrowUtils.throwIf(true, TEST_ENUM, TEST_MESSAGE);
        });

        // 验证枚举和消息（关键：确保异常和入参一致）
        assertEquals(TEST_ENUM, exception.getCodeBindMessageEnums());
        assertEquals(TEST_MESSAGE, exception.getMessage());
    }

    /**
     * 场景：边界值 - 消息为 null → 正常抛异常
     */
    @Test
    void throwIf_messageNull_throwsException() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            ThrowUtils.throwIf(true, TEST_ENUM, null);
        });
        assertNull(exception.getMessage());
    }

}
