package cn.com.edtechhub.worktopicselection.manager.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
/**
 * WebSocket 配置类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Configuration
@EnableWebSocket // 启用 Spring WebSocket 支持
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    /**
     * 定义 WebSocket 服务端点（endpoint）的访问路径 Path
     */
    String path = "/global/message";

    /**
     * 引入 WebSocket 拦截器依赖
     */
    @Resource
    private WebSocketHandshakeInterceptor webSocketInterceptor;

    /**
     * 引入 WebSocket 管理者依赖
     */
    @Resource
    private WebSocketEditHandler webSocketEditHandler;

    @Value("${app.websocket.allowed-origins}")
    private String allowedOrigins;

    /**
     * 注册 WebSocket 端点
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(webSocketEditHandler, this.path) // 把自定义的消息处理器 editHandler 注册到 path 这个端点
                .addInterceptors(webSocketInterceptor) // 给这个 WebSocket 通信加一个握手拦截器, 用于连接前的校验或参数处理
                .setAllowedOrigins(this.getCorsRule()) // 允许所有来源跨域连接(生产环境中最好改成具体域名)
        ;
    }

    /**
     * 允许跨域规则
     */
    private String[] getCorsRule() {
        return StringUtils.commaDelimitedListToStringArray(allowedOrigins);
    }

    /**
     * 打印配置
     */
    @PostConstruct
    public void printConfig() {
        Class<?> clazz = ClassUtils.getUserClass(this); // 获取原始类
        log.debug("[{}] path: {}", clazz.getSimpleName(), this.path);
        log.debug("[{}] getCorsRule: {}", clazz.getSimpleName(), this.getCorsRule());
    }

}
