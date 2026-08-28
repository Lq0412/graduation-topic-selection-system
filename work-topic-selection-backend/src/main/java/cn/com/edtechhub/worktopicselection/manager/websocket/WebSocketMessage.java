package cn.com.edtechhub.worktopicselection.manager.websocket;

import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 消息类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    /**
     * 消息类型
     */
    private Integer typeCode;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 拓展内容
     */
    private JSONObject extend;

}
