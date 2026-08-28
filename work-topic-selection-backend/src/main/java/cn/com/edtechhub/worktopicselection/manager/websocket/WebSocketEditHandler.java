package cn.com.edtechhub.worktopicselection.manager.websocket;

import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.manager.satoken.SaTokenManager;
import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.model.enums.UserRoleEnum;
import cn.com.edtechhub.worktopicselection.service.UserService;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求处理器
 */
@Component
@Slf4j
public class WebSocketEditHandler extends TextWebSocketHandler {

    /**
     * 注入用户服务
     */
    @Resource
    private UserService userService;

    @Resource
    private SaTokenManager saTokenManager;

    /**
     * 保存所有连接的会话, 唯一通话标识
     *                          -> (用户1 ID, 用户会话)
     *                          -> (用户2 ID, 用户会话)
     *                          -> ...
     */
    private final Map<Long, ConcurrentHashMap<Long, WebSocketSession>> sessions = new ConcurrentHashMap<>();

    /**
     * 链接建立后执行的方法
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 获取属性
        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user");
        Long id = (Long) attributes.get("id");

        // 添加集合
        if (Long.valueOf(1L).equals(id) && user != null) {
            sessions.putIfAbsent(id, new ConcurrentHashMap<>());
            WebSocketSession previousSession = sessions.get(id).put(user.getId(), session);
            if (previousSession != null && previousSession != session && previousSession.isOpen()) {
                try {
                    previousSession.close(CloseStatus.NORMAL);
                } catch (Exception e) {
                    log.debug("关闭用户 {} 的旧 WebSocket 连接失败", user.getId(), e);
                }
            }
            log.debug("用户 {} 连接到 id 为 {} 的 WebSocket 链接", user.getUserName(), id);
        }
    }

    /**
     * 链接关闭后执行的方法
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) {
        // 获取属性
        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user");
        Long id = (Long) attributes.get("id");

        // 移除集合
        if (id != null && user != null && sessions.containsKey(id)) {
            ConcurrentHashMap<Long, WebSocketSession> sessions = this.sessions.get(id);
            sessions.remove(user.getId(), session);
            if (sessions.isEmpty()) { // 如果集合为空, 直接删除集合
                this.sessions.remove(id);
            }
            log.debug("用户 {} 断开 id 为 {} 的 WebSocket 连接", user.getUserName(), id);
        }
    }

    /**
     * 编写接收客户消息方法
     */
    @Override
    protected void handleTextMessage(@NotNull WebSocketSession webSocketSession, TextMessage textMessage) throws Exception {
        // 获取消息
        String payload = textMessage.getPayload();
        log.debug("收到 WebSocket 消息: {}", payload);

        try {
            // 获取会话属性
            Map<String, Object> attributes = webSocketSession.getAttributes();

            // 从当前 session 提取 id
            Long id = (Long) attributes.get("id");
            ThrowUtils.throwIf(!Long.valueOf(1L).equals(id), CodeBindMessageEnums.PARAMS_ERROR, "链接频道不存在");

            // 从当前 session 提取 user
            User user = (User) attributes.get("user");
            ThrowUtils.throwIf(user == null, CodeBindMessageEnums.PARAMS_ERROR, "链接上下文中的请求用户信息不存在");
            String tokenValue = (String) attributes.get("token");
            ThrowUtils.throwIf(
                    !saTokenManager.isTokenValidForLoginId(tokenValue, user.getId()),
                    CodeBindMessageEnums.NO_AUTH_ERROR,
                    "登录会话已失效，请重新登录"
            );
            User currentUser = userService.getById(user.getId());
            ThrowUtils.throwIf(currentUser == null, CodeBindMessageEnums.NO_AUTH_ERROR, "用户账号已失效");
            ThrowUtils.throwIf(!userService.userIsAdmin(currentUser), CodeBindMessageEnums.NO_AUTH_ERROR,
                    "仅管理员可以发送全局通知");

            // 转化为结构对象
            ThrowUtils.throwIf(!JSONUtil.isJson(payload), CodeBindMessageEnums.PARAMS_ERROR, "用户消息中, 消息格式错误");
            WebSocketMessage webSocketMessage = JSONUtil.toBean(textMessage.getPayload(), WebSocketMessage.class);

            // 提取消息类型
            Integer typeCode = webSocketMessage.getTypeCode();
            ThrowUtils.throwIf(typeCode == null, CodeBindMessageEnums.PARAMS_ERROR, "用户消息中, 消息类型为空");
            assert typeCode != null;
            WebSocketMessageTypeEnum webSocketMessageTypeEnum = WebSocketMessageTypeEnum.getEnumByCode(typeCode);
            ThrowUtils.throwIf(webSocketMessageTypeEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "用户消息中, 消息类型错误");
            assert webSocketMessageTypeEnum != null;

            // 提取消息内容
            String message = webSocketMessage.getMessage();
            ThrowUtils.throwIf(StringUtils.isBlank(message), CodeBindMessageEnums.PARAMS_ERROR, "用户消息中, 消息内容为空");

            // 广播发送消息
            ConcurrentHashMap<Long, WebSocketSession> sessions = this.sessions.get(id);
            if (sessions != null) {
                for (Map.Entry<Long, WebSocketSession> entry : sessions.entrySet()) {
                    WebSocketSession receiverSession = entry.getValue();
                    if (receiverSession == webSocketSession) {
                        continue;
                    }
                    if (!isActiveSession(receiverSession)) {
                        sessions.remove(entry.getKey(), receiverSession);
                        if (receiverSession.isOpen()) {
                            receiverSession.close(CloseStatus.POLICY_VIOLATION);
                        }
                        continue;
                    }
                    if (receiverSession.isOpen()) {
                        receiverSession.sendMessage(new TextMessage(payload));
                    }
                }
                if (sessions.isEmpty()) {
                    this.sessions.remove(id, sessions);
                }
            }
        }
        catch (BusinessException e) {
            WebSocketMessage errorWebSocketMessage = new WebSocketMessage();
            errorWebSocketMessage.setTypeCode(WebSocketMessageTypeEnum.ERROR_MESSAGE.getCode());
            errorWebSocketMessage.setMessage(e.getCodeBindMessageEnums().getMessage() + ": " + e.getExceptionMessage());
            webSocketSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(errorWebSocketMessage)));
            if (e.getCodeBindMessageEnums() == CodeBindMessageEnums.NO_AUTH_ERROR && webSocketSession.isOpen()) {
                webSocketSession.close(CloseStatus.POLICY_VIOLATION);
            }
        }
    }

    private boolean isActiveSession(WebSocketSession session) {
        if (session == null || !session.isOpen()) {
            return false;
        }
        Map<String, Object> attributes = session.getAttributes();
        User sessionUser = (User) attributes.get("user");
        String tokenValue = (String) attributes.get("token");
        if (sessionUser == null || !saTokenManager.isTokenValidForLoginId(tokenValue, sessionUser.getId())) {
            return false;
        }
        User currentUser = userService.getById(sessionUser.getId());
        UserRoleEnum role = currentUser == null || currentUser.getUserRole() == null
                ? null
                : UserRoleEnum.getEnums(currentUser.getUserRole());
        return role != null && role != UserRoleEnum.BAN_ROLE;
    }

}
