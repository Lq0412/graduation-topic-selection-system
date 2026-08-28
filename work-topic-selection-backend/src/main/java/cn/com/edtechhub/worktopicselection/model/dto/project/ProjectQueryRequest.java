package cn.com.edtechhub.worktopicselection.model.dto.project;

import cn.com.edtechhub.worktopicselection.model.dto.PageRequest;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 项目查询请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class ProjectQueryRequest extends PageRequest implements Serializable {

    private String projectName;

    private String deptName;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
