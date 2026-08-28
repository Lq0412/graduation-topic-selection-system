package cn.com.edtechhub.worktopicselection.response;

import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应体描述类, 确保所有的接口返回都按照下面的类型结构进行返回, 用户只需要定义这个类型在接口返回值即可, 在接口中使用 ThResult 工具来放回响应即可
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class BaseResponse<T> implements Serializable {

    /**
     * 状态
     */
    private int code;

    /**
     * 含义
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 构造方法, 快速封装 成功 响应体(重在数据)
     */
    public BaseResponse(CodeBindMessageEnums codeBindMessageEnums, T data) {
        this.code = codeBindMessageEnums.getCode();
        this.message = codeBindMessageEnums.getMessage();
        this.data = data;
    }

    /**
     * 构造方法, 快速封装 错误 响应体(重在含义)
     */
    public BaseResponse(CodeBindMessageEnums codeBindMessageEnums, String message) {
        this.code = codeBindMessageEnums.getCode();
        this.message = codeBindMessageEnums.getMessage() + ": " + message;
        this.data = null;
    }

    /**
     * 构造方法, 快速封装 临时 响应体(仅供测试)
     */
    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

}