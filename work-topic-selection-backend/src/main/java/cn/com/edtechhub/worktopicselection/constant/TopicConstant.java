package cn.com.edtechhub.worktopicselection.constant;

/**
 * 选题常量
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
public interface TopicConstant {

    /**
     * 默认审核打回理由的最大长度
     */
    Integer MAX_REASON_SIZE = 1024;

    /**
     * 查看选题开关缓存 Key
     */
    String VIEW_TOPIC_SWITCH = "view-topic-switch";

    /**
     * 跨系选题开关缓存 Key
     */
    String CROSS_TOPIC_SWITCH = "cross-topic-switch";

    /**
     * 单选角色开关缓存 Key
     */
    String SWITCH_SINGLE_CHOICE = "switch-single-choice";

    /**
     * 是否退选加锁缓存 key
     */
    String TOPIC_LOCK = "topic-lock";

    /**
     * 是否退选加锁缓存 key
     */
    String TOPIC_LOCK_TIME = "topic-lock-time";

    /**
     * 跨系选题配置缓存 Key
     */
    String DEPT_CROSS_TOPIC_CONFIG = "dept-cross-topic-config";

}
