package cn.com.edtechhub.worktopicselection.manager.websocket;

import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.model.enums.UserRoleEnum;
import cn.com.edtechhub.worktopicselection.service.UserService;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 拦截 WebSocket 的 HTTP 请求携带用户登录信息, 避免后续的请求无法获取用户登录信息
 */
@Component
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * 注入用户服务
     */
    @Resource
    UserService userService;

    /**
     * 在 WebSocket 握手开始前, 也就是 HTTP 升级开始前执行
     * @param request    请求
     * @param response   响应
     * @param wsHandler  处理器
     * @param attributes 属性, 方便后续使用 session.getAttributes() 获取属性
     *
     * @return 是否允许握手的布尔值
     */
    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes) {
        log.debug("WebSocket 握手开始前触发 beforeHandshake(), 其中 {} {} {}", request, response, wsHandler);

        // 如果当前请求基于 Servlet 的 HTTP 请求
        if (!(request instanceof ServletServerHttpRequest)) {
            return false;
        }

        // 获取网络请求对象
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

        // 设置 userVO 参数
        User sessionUser = userService.userGetCurrentLoginUser();
        User user = sessionUser == null || sessionUser.getId() == null
                ? null
                : userService.getById(sessionUser.getId());
        UserRoleEnum role = user == null || user.getUserRole() == null
                ? null
                : UserRoleEnum.getEnums(user.getUserRole());
        if (user == null || role == null || role == UserRoleEnum.BAN_ROLE) {
            log.error("用户尚未登录, 拒绝握手");
            return false;
        }
        attributes.put("user", user);
        String tokenValue = StpUtil.getTokenValue();
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            return false;
        }
        attributes.put("token", tokenValue);

        // 设置 id 参数
        String idStr = servletRequest.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            return false;
        }
        try {
            Long id = Long.parseLong(idStr);
            if (!Long.valueOf(1L).equals(id)) {
                return false;
            }
            attributes.put("id", id);
        } catch (NumberFormatException e) {
            log.debug("无效的 id 参数: {}", idStr);
            return false;
        }

        // 允许握手
        return true;
    }

    /**
     * 在 WebSocket 握手结束后, 也就是 HTTP 升级结束后执行
     *
     * @param request   请求
     * @param response  响应
     * @param wsHandler 处理器
     * @param exception 异常
     */
    @Override
    public void afterHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, Exception exception) {
        log.debug("WebSocket 握手结束后触发 afterHandshake(), 其中 {} {} {}", request, response, wsHandler);
    }

}
