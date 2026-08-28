package cn.com.edtechhub.worktopicselection.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IpUtilsTest {

    @Mock
    private HttpServletRequest request;

    // === 测试 getIpAddress() ===
    /**
     * 场景：给定 x-forwarded-for 头有有效 IP → 返回该 IP
     */
    @Test
    void getIpAddress_givenXForwardedForHasIp_returnsXForwardedForIp() {
        // 给定 x-forwarded-for 头为真实客户端 IP
        String clientIp = "192.168.1.100";
        when(request.getHeader("x-forwarded-for")).thenReturn(clientIp);

        // 执行方法
        String ip = IpUtils.getIpAddress(request);

        // 验证返回 x-forwarded-for 中的 IP
        assertEquals(clientIp, ip);
    }

    /**
     * 场景：x-forwarded-for 为 unknown → 取 Proxy-Client-IP 的有效 IP
     */
    @Test
    void getIpAddress_givenXForwardedForIsUnknown_returnsProxyClientIp() {
        // 给定 x-forwarded-for 为 unknown，Proxy-Client-IP 为有效 IP
        when(request.getHeader("x-forwarded-for")).thenReturn("unknown");
        String proxyClientIp = "10.0.0.1";
        when(request.getHeader("Proxy-Client-IP")).thenReturn(proxyClientIp);

        // 执行方法
        String ip = IpUtils.getIpAddress(request);

        // 验证返回 Proxy-Client-IP 的 IP
        assertEquals(proxyClientIp, ip);
    }

    /**
     * 场景：x-forwarded-for 和 Proxy-Client-IP 均为 unknown → 取 WL-Proxy-Client-IP 的有效 IP
     */
    @Test
    void getIpAddress_givenXForwardedForAndProxyClientIpIsUnknown_returnsWLProxyClientIp() {
        // 给定 x-forwarded-for 和 Proxy-Client-IP 均为 unknown，WL-Proxy-Client-IP 为有效 IP
        when(request.getHeader("x-forwarded-for")).thenReturn("unknown");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("unknown");
        String wlProxyClientIp = "172.16.0.1";
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(wlProxyClientIp);

        // 执行方法
        String ip = IpUtils.getIpAddress(request);

        // 验证返回 WL-Proxy-Client-IP 的 IP
        assertEquals(wlProxyClientIp, ip);
    }

    /**
     * 场景：前三类头均为 unknown 且 remoteAddr 是 127.0.0.1 → 返回本机 IP
     */
    @Test
    void getIpAddress_givenAllHeadersUnknownAndRemoteAddrIsLocalhost_returnsLocalHostIp() {
        // 给定前三类头均为 unknown，remoteAddr 为 127.0.0.1
        when(request.getHeader("x-forwarded-for")).thenReturn("unknown");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("unknown");
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("unknown");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // 执行方法（InetAddress.getLocalHost() 会返回本机真实 IP，如 192.168.xx.xx）
        String ip = IpUtils.getIpAddress(request);

        // 验证返回的不是 127.0.0.1（而是本机实际 IP）
        assert !ip.equals("127.0.0.1");
    }

    /**
     * 场景：前三类头均为 unknown 且 remoteAddr 是有效 IP → 返回 remoteAddr 的 IP
     */
    @Test
    void getIpAddress_givenAllHeadersUnknownAndRemoteAddrIsValid_returnsRemoteAddrIp() {
        // 给定前三类头均为 unknown，remoteAddr 为有效 IP
        when(request.getHeader("x-forwarded-for")).thenReturn("unknown");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("unknown");
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("unknown");
        String remoteIp = "203.0.113.5";
        when(request.getRemoteAddr()).thenReturn(remoteIp);

        // 执行方法
        String ip = IpUtils.getIpAddress(request);

        // 验证返回 remoteAddr 的 IP
        assertEquals(remoteIp, ip);
    }

    /**
     * 场景：x-forwarded-for 包含多个 IP（逗号分隔）→ 返回第一个 IP
     */
    @Test
    void getIpAddress_givenXForwardedForHasMultipleIp_returnsFirstIp() {
        // 给定 x-forwarded-for 包含多个 IP
        String multipleIp = "192.168.1.100,10.0.0.1,172.16.0.1";
        when(request.getHeader("x-forwarded-for")).thenReturn(multipleIp);

        // 执行方法
        String ip = IpUtils.getIpAddress(request);

        // 验证返回第一个 IP
        assertEquals("192.168.1.100", ip);
    }

    /**
     * 场景：所有途径都获取不到 IP（最终 ip 为 null）→ 返回 127.0.0.1
     */
    @Test
    void getIpAddress_givenAllIpIsNull_returnsLocalhost() {
        // 模拟极端场景：所有头为 null，remoteAddr 为 null（需结合代码逻辑模拟）
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(null);

        // 执行方法
        String ip = IpUtils.getIpAddress(request);

        // 验证返回 127.0.0.1
        assertEquals("127.0.0.1", ip);
    }

    /**
     * 场景：x-forwarded-for 为空字符串 → 取 Proxy-Client-IP 的有效 IP
     */
    @Test
    void getIpAddress_givenXForwardedForIsEmpty_returnsProxyClientIp() {
        // 给定 x-forwarded-for 为空字符串，Proxy-Client-IP 为有效 IP
        when(request.getHeader("x-forwarded-for")).thenReturn("");
        String proxyClientIp = "10.0.0.2";
        when(request.getHeader("Proxy-Client-IP")).thenReturn(proxyClientIp);

        // 执行方法
        String ip = IpUtils.getIpAddress(request);

        // 验证返回 Proxy-Client-IP 的 IP
        assertEquals(proxyClientIp, ip);
    }

}
