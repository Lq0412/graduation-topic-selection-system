package cn.com.edtechhub.worktopicselection.exception;

import cn.com.edtechhub.worktopicselection.response.BaseResponse;
import cn.com.edtechhub.worktopicselection.response.TheResult;
import cn.dev33.satoken.exception.DisableServiceException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理方法类
 * 截获异常, 把异常的 "错误-含义:消息" 作为响应传递给前端, 本质时为了避免让服务层抛异常而不涉及报文相关的东西, 让全局异常处理器来代做
 * Java 异常体系
 * Object -> Throwable -> 错误: Error && 异常: Exception
 * -> 运行时异常: RuntimeException(BusinessException, NotLoginException, NotPermissionException, NotRoleException, DisableServiceException)
 * -> 非运时异常: IOException
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@RestControllerAdvice
// 使用 @RestControllerAdvice 可以拦截所有抛出的异常, 并统一返回 JSON 格式的错误信息, 这里还支持了异常熔断, 避免异常被提前处理后无法上报
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 全局所有异常处理方法(兜底把所有运行时异常拦截后进行处理)
     */
    @ExceptionHandler
    // 直接拦截控制层及其内部调用的所有 Throwable
    public BaseResponse<String> exceptionHandler(Exception e) {
        log.error("触发全局所有异常处理方法");
        printStackTraceStatus(e, 0);
        return TheResult.error(CodeBindMessageEnums.SYSTEM_ERROR, "请联系系统管理员");
    }

    /**
     * 业务内部异常处理方法(服务层手动使用)
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.warn("触发业务内部异常处理方法");
        printStackTraceStatus(e, 1);
        return TheResult.error(e.getCodeBindMessageEnums(), e.exceptionMessage);
    }

    /**
     * 登录认证异常处理方法(由 Sa-token 框架自己来触发)
     */
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginExceptionHandler() {
        log.warn("触发登录认证异常处理方法");
        return TheResult.error(CodeBindMessageEnums.NO_LOGIN_ERROR, "请先进行登录");
    }

    /**
     * 权限认证异常处理方法(角色标识认证, 由 Sa-token 框架自己来触发)
     */
    @ExceptionHandler(NotRoleException.class)
    public BaseResponse<?> notRoleExceptionHandler() {
        log.warn("触发权限认证异常处理方法(角色标识认证)");
        return TheResult.error(CodeBindMessageEnums.NO_ROLE_ERROR, "用户当前角色不允许使用该功能");
    }

    /**
     * 权限认证异常处理方法(权限码值认证, 由 Sa-token 框架自己来触发)
     */
    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> notPermissionExceptionHandler() {
        log.warn("触发权限认证异常处理方法(权限码值认证)");
        return TheResult.error(CodeBindMessageEnums.NO_AUTH_ERROR, "用户当前权限不允许使用该功能, 请申请权限");
    }

    /**
     * 用户封禁异常处理方法(由 Sa-token 框架自己来触发)
     */
    @ExceptionHandler(DisableServiceException.class)
    public BaseResponse<?> disableServiceExceptionHandler() {
        log.warn("触发用户封禁异常处理方法");
        return TheResult.error(CodeBindMessageEnums.USER_DISABLE_ERROR, "当前用户因为违规被封禁");
    }

    /**
     * 流量控制异常处理方法(由 Sentinel 框架自己来触发)
     */
    @ExceptionHandler(BlockException.class)
    // TODO: 不过由于服务层和控制层做双重的验证很累, 在单体项目中就不需要使用这个
    public BaseResponse<?> handleBlockExceptionHandler() {
        log.warn("触发流量控制异常处理方法");
        return TheResult.error(CodeBindMessageEnums.FLOW_RULES, "请求流量过大, 请稍后再试");
    }

    /**
     * 参数校验异常处理方法(由 Hibernate Validator 框架自己来触发)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // TODO: 不过由于服务层和控制层做双重的验证很累, 在单体项目中就不需要使用这个
    public BaseResponse<?> handleValidationException(MethodArgumentNotValidException ex) {
        // 获取第一个字段错误
        String errorMessage = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map((error) -> {
                    log.warn("{}", error.getField() + " - " + error.getDefaultMessage());
                    return error.getDefaultMessage();
                })
                .orElse("请求参数校验失败"); // 兜底提示
        return TheResult.error(CodeBindMessageEnums.PARAMS_ERROR, errorMessage);
    }

    /**
     * 打印异常定位
     */
    private void printStackTraceStatus(Exception e, int tier) {
        StackTraceElement element = e.getStackTrace()[tier];
        // 获取异常抛出位置(第一个堆栈元素)
        log.warn("异常位置: {} -> 文件: {}, 方法: {}, 码行: {}",
                element.getFileName(),
                element.getClassName(),
                element.getMethodName(),
                element.getLineNumber()
        );
    }

}
