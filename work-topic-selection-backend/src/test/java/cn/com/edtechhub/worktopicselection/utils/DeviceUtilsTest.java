package cn.com.edtechhub.worktopicselection.utils;

import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.hutool.http.Header;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceUtilsTest {

    @Mock
    private HttpServletRequest request;

    // === 测试 getRequestDevice() ===
    /**
     * 场景：给定 PC 端 UserAgent → 返回 "pc"
     */
    @Test
    void getRequestDevice_givenPcUserAgent_returnsPc() {
        // 给定 PC 端 UserAgent
        String pcUserAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36";
        when(request.getHeader(Header.USER_AGENT.toString())).thenReturn(pcUserAgent);

        // 执行方法
        String device = DeviceUtils.getRequestDevice(request);

        // 验证返回"pc"
        assertEquals("pc", device);
    }

    /**
     * 场景：给定微信小程序 UserAgent → 返回 "miniProgram"
     */
    @Test
    void getRequestDevice_givenMiniProgramUserAgent_returnsMiniProgram() {
        String miniProgramUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) MicroMessenger/8.0.50 MiniProgram NetType/WIFI";
        when(request.getHeader(Header.USER_AGENT.toString())).thenReturn(miniProgramUserAgent);

        String device = DeviceUtils.getRequestDevice(request);
        assertEquals("miniProgram", device);
    }

    /**
     * 场景：给定 iPad UserAgent → 返回"pad"
     */
    @Test
    void getRequestDevice_givenIpadUserAgent_returnsPad() {
        String ipadUserAgent = "Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15";
        when(request.getHeader(Header.USER_AGENT.toString())).thenReturn(ipadUserAgent);

        String device = DeviceUtils.getRequestDevice(request);
        assertEquals("pad", device);
    }

    /**
     * 场景：给定 Android 平板 UserAgent → 返回"pad"
     */
    @Test
    void getRequestDevice_givenAndroidTabletUserAgent_returnsPad() {
        String androidTabletUserAgent = "Mozilla/5.0 (Linux; Android 14; Lenovo Tab P12 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36";
        when(request.getHeader(Header.USER_AGENT.toString())).thenReturn(androidTabletUserAgent);

        String device = DeviceUtils.getRequestDevice(request);
        assertEquals("pad", device);
    }

    /**
     * 场景：给定手机端 UserAgent → 返回"mobile"
     */
    @Test
    void getRequestDevice_givenMobileUserAgent_returnsMobile() {
        String mobileUserAgent = "Mozilla/5.0 (Linux; Android 14; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Mobile Safari/537.36";
        when(request.getHeader(Header.USER_AGENT.toString())).thenReturn(mobileUserAgent);

        String device = DeviceUtils.getRequestDevice(request);
        assertEquals("mobile", device);
    }

    /**
     * 场景：给定空 UserAgent → 抛出 BusinessException
     */
    @Test
    void getRequestDevice_givenNullUserAgent_throwsBusinessException() {
        when(request.getHeader(Header.USER_AGENT.toString())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            DeviceUtils.getRequestDevice(request);
        });

        assertEquals(CodeBindMessageEnums.PARAMS_ERROR.getCode(), exception.getCodeBindMessageEnums().getCode());
        assertEquals(CodeBindMessageEnums.PARAMS_ERROR.getMessage(), exception.getCodeBindMessageEnums().getMessage());
    }

    // === 测试 getRequestDeviceInfo() ===
    /**
     * 场景：给定正常 UserAgent → 返回完整 UserAgent 字符串
     */
    @Test
    void getRequestDeviceInfo_givenNormalUserAgent_returnsUserAgentString() {
        String testUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Edg/143.0.0.0";
        when(request.getHeader(Header.USER_AGENT.toString())).thenReturn(testUserAgent);

        String deviceInfo = DeviceUtils.getRequestDeviceInfo(request);
        assertEquals(testUserAgent, deviceInfo);
    }

    /**
     * 场景：给定空 UserAgent → 返回 null
     */
    @Test
    void getRequestDeviceInfo_givenNullUserAgent_returnsNull() {
        when(request.getHeader(Header.USER_AGENT.toString())).thenReturn(null);

        String deviceInfo = DeviceUtils.getRequestDeviceInfo(request);
        assertNull(deviceInfo);
    }

}
