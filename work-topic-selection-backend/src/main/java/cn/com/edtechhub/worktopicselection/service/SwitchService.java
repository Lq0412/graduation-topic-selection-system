package cn.com.edtechhub.worktopicselection.service;

/**
 * 开关服务接口
 */
public interface SwitchService {

    /**
     * 是否设置
     */
    boolean isEnabled(String key);

    /**
     * 设置开关
     */
    void setEnabled(String key, boolean enabled);

}
