package cn.com.edtechhub.worktopicselection.exception;

import lombok.Getter;

/**
 * 业务内异常类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Getter
public class BusinessException extends RuntimeException { /* RuntimeException 会触发事务回滚 */

    /**
     * 错误-含义
     */
    CodeBindMessageEnums codeBindMessageEnums;

    /**
     * 详细信息
     */
    String exceptionMessage;

    /**
     * 构造异常对象
     *
     * @param codeBindMessageEnums 错误-含义 枚举体
     * @param exceptionMessage 详细信息
     */
    public BusinessException(CodeBindMessageEnums codeBindMessageEnums, String exceptionMessage) {
        super(exceptionMessage);
        this.codeBindMessageEnums = codeBindMessageEnums;
        this.exceptionMessage = exceptionMessage;
    }

}
