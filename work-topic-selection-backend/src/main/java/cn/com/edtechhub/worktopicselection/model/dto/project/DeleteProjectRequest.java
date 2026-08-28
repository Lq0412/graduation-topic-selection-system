package cn.com.edtechhub.worktopicselection.model.dto.project;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class DeleteProjectRequest implements Serializable {

    /**
     * id
     */
    private String projectName;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}