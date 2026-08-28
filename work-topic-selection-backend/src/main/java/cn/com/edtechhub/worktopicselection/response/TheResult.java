package cn.com.edtechhub.worktopicselection.response;

import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;

/**
 * 便捷响应体工具类
 * 1. 返回成功, 自动处理, {code: 20000, message: "成功", data: { 您来定夺 }}
 * 2. 返回失败, 自动处理, {code: xxxxx, message: "xxxxxxx: xxx"}
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
public class TheResult {

    /**
     * 构造成功响应体(重在数据)
     */
    public static <T> BaseResponse<T> success(CodeBindMessageEnums codeBindMessageEnums, T data) {
        return new BaseResponse<>(codeBindMessageEnums, data);
    }

    /**
     * 构造失败响应体(重在消息)
     */
    public static <T> BaseResponse<T> error(CodeBindMessageEnums codeBindMessageEnums, String message) {
        return new BaseResponse<>(codeBindMessageEnums, message);
    }

    /**
     * 构造等待开发响应体
     */
    public static <T> BaseResponse<T> notyet() {
        return new BaseResponse<>(-1, "该接口尚在开发中", null);
    }
    public static <T> BaseResponse<T> notyet(String test) {
        return new BaseResponse<>(-1, "该接口尚在开发中: " + test, null);
    }

}
