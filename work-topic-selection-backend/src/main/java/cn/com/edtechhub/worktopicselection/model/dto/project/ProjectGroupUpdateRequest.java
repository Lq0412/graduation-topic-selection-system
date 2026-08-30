package cn.com.edtechhub.worktopicselection.model.dto.project;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新专业选题组请求。
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class ProjectGroupUpdateRequest implements Serializable {

    /**
     * 专业名称。
     */
    private String projectName;

    /**
     * 选题组名称；为空表示取消该专业的分组配置。
     */
    private String groupName;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
