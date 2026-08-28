package cn.com.edtechhub.worktopicselection.manager.websocket;

import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.manager.satoken.SaTokenManager;
import cn.com.edtechhub.worktopicselection.model.enums.UserRoleEnum;
import cn.com.edtechhub.worktopicselection.service.UserService;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebSocketEditHandlerTest {

    private WebSocketEditHandler handler;
    private UserService userService;

    private SaTokenManager saTokenManager;

    @BeforeEach
    void setUp() {
        handler = new WebSocketEditHandler();
        userService = mock(UserService.class);
        saTokenManager = mock(SaTokenManager.class);
        ReflectionTestUtils.setField(handler, "userService", userService);
        ReflectionTestUtils.setField(handler, "saTokenManager", saTokenManager);
    }

    @Test
    void handleTextMessage_givenNonAdmin_sendsJsonErrorWithoutBroadcasting() throws Exception {
        User sender = user(1L);
        User receiver = user(2L);
        WebSocketSession senderSession = session(sender, 1L);
        WebSocketSession receiverSession = session(receiver, 1L);
        handler.afterConnectionEstablished(receiverSession);
        when(saTokenManager.isTokenValidForLoginId("token-1", sender.getId())).thenReturn(true);
        when(userService.getById(sender.getId())).thenReturn(sender);
        when(userService.userIsAdmin(sender)).thenReturn(false);

        handler.handleTextMessage(senderSession, validMessage());

        ArgumentCaptor<TextMessage> errorCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(senderSession).sendMessage(errorCaptor.capture());
        verify(receiverSession, never()).sendMessage(any(TextMessage.class));
        JSONObject error = JSONUtil.parseObj(errorCaptor.getValue().getPayload());
        assertEquals(WebSocketMessageTypeEnum.ERROR_MESSAGE.getCode(), error.getInt("typeCode"));
        assertEquals("权限认证错误: 仅管理员可以发送全局通知", error.getStr("message"));
    }

    @Test
    void handleTextMessage_givenAdmin_broadcastsToLoggedInReceiver() throws Exception {
        User sender = user(1L);
        User receiver = user(2L);
        WebSocketSession senderSession = session(sender, 1L);
        WebSocketSession receiverSession = session(receiver, 1L);
        handler.afterConnectionEstablished(receiverSession);
        when(userService.userIsAdmin(sender)).thenReturn(true);
        when(saTokenManager.isTokenValidForLoginId("token-1", sender.getId())).thenReturn(true);
        when(saTokenManager.isTokenValidForLoginId("token-2", receiver.getId())).thenReturn(true);
        when(userService.getById(sender.getId())).thenReturn(sender);
        when(userService.getById(receiver.getId())).thenReturn(receiver);
        TextMessage message = validMessage();

        handler.handleTextMessage(senderSession, message);

        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(receiverSession).sendMessage(messageCaptor.capture());
        verify(senderSession, never()).sendMessage(any(TextMessage.class));
        assertEquals(message.getPayload(), messageCaptor.getValue().getPayload());
    }

    @Test
    void handleTextMessage_givenLoggedOutReceiver_closesWithoutBroadcasting() throws Exception {
        User sender = user(1L);
        User receiver = user(2L);
        WebSocketSession senderSession = session(sender, 1L);
        WebSocketSession receiverSession = session(receiver, 1L);
        handler.afterConnectionEstablished(receiverSession);
        when(userService.userIsAdmin(sender)).thenReturn(true);
        when(saTokenManager.isTokenValidForLoginId("token-1", sender.getId())).thenReturn(true);
        when(saTokenManager.isTokenValidForLoginId("token-2", receiver.getId())).thenReturn(false);
        when(userService.getById(sender.getId())).thenReturn(sender);

        handler.handleTextMessage(senderSession, validMessage());

        verify(receiverSession, never()).sendMessage(any(TextMessage.class));
        verify(receiverSession).close(CloseStatus.POLICY_VIOLATION);
    }

    private User user(long id) {
        User user = new User();
        user.setId(id);
        user.setUserName("user-" + id);
        user.setUserRole(UserRoleEnum.STUDENT.getCode());
        return user;
    }

    private WebSocketSession session(User user, long channelId) {
        WebSocketSession session = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("user", user);
        attributes.put("id", channelId);
        attributes.put("token", "token-" + user.getId());
        when(session.getAttributes()).thenReturn(attributes);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    private TextMessage validMessage() {
        WebSocketMessage message = new WebSocketMessage();
        message.setTypeCode(WebSocketMessageTypeEnum.INFO_MESSAGE.getCode());
        message.setMessage("notice");
        return new TextMessage(JSONUtil.toJsonStr(message));
    }
}
