package cn.com.edtechhub.worktopicselection.manager.sentine;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis 管理类
 */
@Component
@Slf4j
@Data
public class SentineManager {

    /**
     * 注入 SentineConfig 配置依赖
     */
    @Resource
    private SentineConfig sentineConfig;

    private final Map<String, FlowRule> flowRules = new ConcurrentHashMap<>();

    /**
     * 初始化限流规则
     *
     * @param entryName 资源名称
     * @param count     限流次数
     */
    public void initFlowRules(String entryName, Integer count) {
        upsertFlowRule(entryName, count.doubleValue());
    }

    /**
     * 初始化限流规则
     *
     * @param entryName 资源名称
     */
    public void initFlowRules(String entryName) {
        upsertFlowRule(entryName, sentineConfig.getQps());
    }

    private synchronized void upsertFlowRule(String entryName, double count) {
        FlowRule current = flowRules.get(entryName);
        if (current != null && Double.compare(current.getCount(), count) == 0) {
            return;
        }
        FlowRule rule = new FlowRule();
        rule.setResource(entryName);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(count);
        flowRules.put(entryName, rule);
        FlowRuleManager.loadRules(new ArrayList<>(flowRules.values()));
    }

}
