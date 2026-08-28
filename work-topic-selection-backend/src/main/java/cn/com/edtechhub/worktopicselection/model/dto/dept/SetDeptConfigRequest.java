package cn.com.edtechhub.worktopicselection.model.dto.dept;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 设置系部配置请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class SetDeptConfigRequest implements Serializable {

    /**
     * 可选系部 ID 配置列表
     */
    private Map<String, List<String>> enableSelectDeptsList;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
