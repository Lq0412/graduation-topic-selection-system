package cn.com.edtechhub.worktopicselection.utils;

import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import lombok.extern.slf4j.Slf4j;

/**
 * 异常处理工具类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Slf4j
public class ThrowUtils {

    /**
     * 条件成立则抛异常，并且打印消息日志
     */
    public static void throwIf(boolean condition, CodeBindMessageEnums codeBindMessageEnums, String message) {
        if (condition) {
            log.warn(message);
            throw new BusinessException(codeBindMessageEnums, message);
        }
    }

}
