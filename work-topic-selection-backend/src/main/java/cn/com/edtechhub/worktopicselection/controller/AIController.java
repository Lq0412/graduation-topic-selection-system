package cn.com.edtechhub.worktopicselection.controller;

import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.manager.sentine.SentineManager;
import cn.com.edtechhub.worktopicselection.model.dto.ai.AiSendRequest;
import cn.com.edtechhub.worktopicselection.response.BaseResponse;
import cn.com.edtechhub.worktopicselection.response.TheResult;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 智能控制层
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@RestController
@RequestMapping("/ai")
public class AIController {

    /**
     * 注入流量控制依赖
     */
    @Resource
    private SentineManager sentineManager;

    /**
     * 快速提供 AI 询问接口
     */
    @SaCheckLogin
    @SaCheckRole(value = {"student"}, mode = SaMode.OR)
    @PostMapping("/send")
    public BaseResponse<String> aiSend(@RequestBody AiSendRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 检查参数
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String content = request.getContent();
        ThrowUtils.throwIf(StringUtils.isBlank(content), CodeBindMessageEnums.PARAMS_ERROR, "请不要发送空消息");

        return TheResult.notyet("AI 问答功能暂未开放");
    }

}
