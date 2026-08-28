package cn.com.edtechhub.worktopicselection.constant;

/**
 * Redis key 常量
 */
public interface RedisConstant {

    /**
     * {项目缩写}:{查询接口}:{查询条件} -> {查询结果}
     */
    String SEARCH_KEY_PREFIX  = "work-topic-selection:search:";

}
