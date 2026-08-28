package cn.com.edtechhub.worktopicselection.manager.websocket;

import lombok.Getter;

/**
 * WebSocket 消息类型枚举
 */
@Getter
public enum WebSocketMessageTypeEnum {

    /**
     * 错误消息
     */
    ERROR_MESSAGE(-1, "ERROR"),

    /**
     * 信息通知
     */
    INFO_MESSAGE(0, "INFO"),

    /**
     * 状态变更
     */
    CHANGE_MESSAGE(1, "STATUS_CHANGE");

    /**
     * 编码
     */
    private final int code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 构造函数
     *
     * @param code 编码
     * @param description 描述
     */
    WebSocketMessageTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     */
    public static WebSocketMessageTypeEnum getEnumByCode(int code) {
        for (WebSocketMessageTypeEnum enumItem : WebSocketMessageTypeEnum.values()) {
            if (enumItem.getCode() == code) {
                return enumItem;
            }
        }
        return null;
    }

}
