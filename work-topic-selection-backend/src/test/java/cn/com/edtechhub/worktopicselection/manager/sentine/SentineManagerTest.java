package cn.com.edtechhub.worktopicselection.manager.sentine;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SentineManagerTest {

    @AfterEach
    void clearGlobalRules() {
        FlowRuleManager.loadRules(Collections.emptyList());
    }

    @Test
    void addingAResourceDoesNotReplaceExistingRules() {
        SentineManager manager = new SentineManager();

        manager.initFlowRules("first-resource", 10);
        manager.initFlowRules("second-resource", 20);

        List<String> resources = FlowRuleManager.getRules()
                .stream()
                .map(FlowRule::getResource)
                .collect(Collectors.toList());
        assertTrue(resources.contains("first-resource"));
        assertTrue(resources.contains("second-resource"));
    }
}
