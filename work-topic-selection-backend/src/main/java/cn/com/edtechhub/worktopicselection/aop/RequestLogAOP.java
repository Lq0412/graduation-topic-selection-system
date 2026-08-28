package cn.com.edtechhub.worktopicselection.aop;

import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.manager.redis.RedisManager;
import cn.com.edtechhub.worktopicselection.utils.DeviceUtils;
import cn.com.edtechhub.worktopicselection.utils.IpUtils;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 请求日志拦截切面, 允许在客户端请求后不断快速获取日志, 同时动态检测访问 IP, 对恶意流量进行封号
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Component
@Slf4j
public class RequestLogAOP implements HandlerInterceptor {

    /**
     * 注入 Redis 管理器
     */
    @Resource
    private RedisManager redisManager;

    /**
     * 最多请求次数
     */
    private static final int MAX_REQUESTS = 350;

    /**
     * 统计窗口秒数
     */
    private static final int TIME_WINDOW_SECONDS = 50;

    /**
     * 封禁时间秒数
     */
    private static final int BAN_TIME_SECONDS = 60;

    /**
     * 每次网络接口被调用都会执行这个方法, 进而打印日志
     */
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        // 打印来源日志信息
        String device = DeviceUtils.getRequestDevice(request);
        String ip = IpUtils.getIpAddress(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String loginId = "外来访客";
        if (StpUtil.isLogin()) {
            loginId = StpUtil.getLoginIdAsString();
        }
        int count = 0;
        Date unbanDate = null;
        String deviceInfo = DeviceUtils.getRequestDeviceInfo(request);

        // 设置恶意流量拦截器(不要对外来访客进行拦截, 否则有可能把整个学校的 ip 都屏蔽)
        if (!loginId.equals("外来访客")) {
            // 设置 Redis 键名
            String redisKey = "user:requests:" + loginId;

            // 获取当前请求次数
            String countStr = redisManager.getValue(redisKey);
            count = countStr == null ? 0 : Integer.parseInt(countStr);

            // 设置白名单
            List<Long> ids = new ArrayList<>(); // 把 9 - 16 的用户加入白名单
            for (long i = 1; i <= 16; i++) {
                ids.add(i);
            }

            // 不在白名单中, 并且超过限制就封禁
            if (count >= MAX_REQUESTS && !ids.contains(Long.parseLong(loginId))) {
                // 获取解禁日期
                long remainingSeconds = StpUtil.getDisableTime(loginId); // 剩余封禁秒数
                long unbanTimestamp = System.currentTimeMillis() + remainingSeconds * 1000; // 毫秒时间戳
                unbanDate = new Date(unbanTimestamp); // 转为日期对象
                if (remainingSeconds > 0) {
                    ThrowUtils.throwIf(true, CodeBindMessageEnums.USER_DISABLE_ERROR, "您的帐号被封禁中, 请等待一段时间再操作, 将在 " + unbanDate + " 解封, 请不要恶意访问本站");
                }

                // 短暂封禁用户
                StpUtil.disable(loginId, BAN_TIME_SECONDS);
                log.info("[RequestLogAOP] 拦截到请求, 来自: {} {} == {} {} {} {}_{} {} == {}", ip, device, loginId, method, uri, count, MAX_REQUESTS, unbanDate, deviceInfo);
                ThrowUtils.throwIf(true, CodeBindMessageEnums.USER_DISABLE_ERROR, "您的帐号被封禁中, 请等待一段时间再操作, 将在 " + unbanDate + " 解封, 请不要恶意访问本站");
            } else {
                // 继续更新键值
                redisManager.setValue(redisKey, String.valueOf(count + 1), TIME_WINDOW_SECONDS);
            }
        }
        log.info("[RequestLogAOP] 拦截到请求, 来自: {} {} == {} {} {} {}_{} {} == {}", ip, device, loginId, method, uri, count, MAX_REQUESTS, "not-ban", deviceInfo);
        return true; // 返回 false 会终止请求, 可以利用这一点进行 IP 屏蔽
    }

}
