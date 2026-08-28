package cn.com.edtechhub.worktopicselection.manager.ai;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 响应模型，用于映射接口返回的四个 data 字段内容
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class AIResponse {

    // 存储第一个 reply 类型的 data(用户提问回显)
    private Map<String, Object> firstReply = new HashMap<>();

    // 存储第一个 token_stat 类型的 data(模型开始处理)
    private Map<String, Object> firstTokenStat = new HashMap<>();

    // 存储第二个 token_stat 类型的 data(推理过程信息)
    private Map<String, Object> secondTokenStat = new HashMap<>();

    // 存储第二个 reply 类型的 data(最终回答内容)
    private Map<String, Object> secondReply = new HashMap<>();

}
