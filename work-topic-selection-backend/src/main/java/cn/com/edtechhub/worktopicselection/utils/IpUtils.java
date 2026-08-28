package cn.com.edtechhub.worktopicselection.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

/**
 * 地址工具类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Slf4j
public class IpUtils {

    /**
     * 获取客户端 IP 地址方法
     * 只做了简单的判断, 如果需要更加复杂的逻辑就需要自己定制化
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");

        // 处理真实用户的情况
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip != null && ip.equals("127.0.0.1")) {
                // 根据网卡取本机配置的 IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    log.error("获取本机 IP 失败", e);
                }
                if (inet != null) {
                    ip = inet.getHostAddress();
                }
            }
        }

        // 处理多个代理的情况
        if (ip != null && ip.length() > 15) {
            // 第一个 IP 为客户端真实 IP，后面多个 IP 则按照 ',' 分割
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }

        // 处理本地测试的情况
        if (ip == null) {
            return "127.0.0.1";
        }

        return ip;
    }

}
