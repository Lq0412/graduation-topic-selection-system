package cn.com.edtechhub.worktopicselection.manager.ai;

import lombok.Data;

/**
 * 调用 AI 对话的最终结果
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class AIResult {

    /**
     * 0-1-2 相似程度(越大越相似)
     */
    private String level;

    /**
     * 解析描述
     */
    private String description;

}
