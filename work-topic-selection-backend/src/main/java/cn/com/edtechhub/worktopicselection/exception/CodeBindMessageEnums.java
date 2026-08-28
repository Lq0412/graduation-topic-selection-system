package cn.com.edtechhub.worktopicselection.exception;

import lombok.Getter;

/**
 * 自定义 错误-含义 枚举体, 对于所有的请求几乎都只返回 200 响应, 但是我们把更加详细的 错误-含义 加入到了响应体中会更加灵活
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Getter // 等价于只能用在类上的 Data
public enum CodeBindMessageEnums {

    /**
     * 枚举常量
     */
    // 2xxxx 处理已成功类型
    SUCCESS(0, "成功"),

    // 3xxxx 重定向地址类型
    // 待补充...

    // 4xxxx 客户端错误类型
    PARAMS_ERROR(40000, "参数错误"),
    USER_INIT_PASSWD(40001, "用户首次登陆"),
    NO_LOGIN_ERROR(40100, "登录认证错误"),
    NO_ROLE_ERROR(40101, "角色认证错误"),
    NO_AUTH_ERROR(40102, "权限认证错误"),
    USER_DISABLE_ERROR(40103, "账号封禁错误"),
    FORBIDDEN_ERROR(40300, "禁止访问的资源"),
    ILLEGAL_OPERATION_ERROR(40301, "您做了非法操作"),
    NOT_FOUND_ERROR(40400, "请求不存在资源"),
    TIMEOUT_ERROR(40800, "请求超时"),

    // 5xxxx 服务端错误类型
    SYSTEM_ERROR(50000, "系统内部错误"),
    OPERATION_ERROR(50001, "操作失败"),
    FLOW_RULES(50002, "触发流量控制规则"),
    DEGRADE_RULES(50003, "触发熔断降级规则"),
    PARAM_RULES(50004, "触发热点参数规则"),
    SYSTEM_RULES(50005, "触发系统保护规则"),

    ;

    /**
     * 状态
     */
    private final int code;

    /**
     * 含义
     */
    private final String message;

    /**
     * 内部构造方法, 可以自定义其他的状态及其含义
     */
    CodeBindMessageEnums(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
