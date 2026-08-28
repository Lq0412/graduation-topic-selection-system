package cn.com.edtechhub.worktopicselection.utils;

import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * 设备工具类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Slf4j
public class DeviceUtils {

    /**
     * 根据请求获取简单设备类型信息
     */
    public static String getRequestDevice(HttpServletRequest request) {
        String userAgentStr = request.getHeader(Header.USER_AGENT.toString());

        // 使用 Hutool 解析 UserAgent
        UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
        if (userAgent == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "禁止隐藏设备类型");
        }

        // 判断设备类型
        String device = "pc"; // 是否为 PC
        if (DeviceUtils.isMiniProgram(userAgentStr)) {
            device = "miniProgram"; // 是否为小程序
        } else if (DeviceUtils.isPad(userAgentStr)) {
            device = "pad"; // 是否为 Pad
        } else if (userAgent.isMobile()) {
            device = "mobile"; // 是否为手机
        }

        log.debug("检测一次设备类型为 {}", device);

        return device;
    }

    /**
     * 根据请求获取详细设备类型信息
     */
    public static String getRequestDeviceInfo(HttpServletRequest request) {
        String userAgentStr = request.getHeader(Header.USER_AGENT.toString());
        log.debug("检测一次设备信息为 {}", userAgentStr);
        return userAgentStr;
    }

    /**
     * 判断是否是小程序
     * 一般通过 User-Agent 字符串中的 "MicroMessenger" 来判断是否是微信小程序
     **/
    private static boolean isMiniProgram(String userAgentStr) {
        // 判断 User-Agent 是否包含 "MicroMessenger" 表示是微信环境
        return StrUtil.containsIgnoreCase(userAgentStr, "MicroMessenger")
                && StrUtil.containsIgnoreCase(userAgentStr, "MiniProgram");
    }

    /**
     * 判断是否为平板设备
     * 支持 iOS(如 iPad)和 Android 平板的检测
     **/
    private static boolean isPad(String userAgentStr) {
        // 检查 iPad 的 User-Agent 标志
        boolean isIpad = StrUtil.containsIgnoreCase(userAgentStr, "iPad");

        // 检查 Android 平板(包含 "Android" 且不包含 "Mobile")
        boolean isAndroidTablet = StrUtil.containsIgnoreCase(userAgentStr, "Android")
                && !StrUtil.containsIgnoreCase(userAgentStr, "Mobile");

        // 如果是 iPad 或 Android 平板, 则返回 true
        return isIpad || isAndroidTablet;
    }

}
