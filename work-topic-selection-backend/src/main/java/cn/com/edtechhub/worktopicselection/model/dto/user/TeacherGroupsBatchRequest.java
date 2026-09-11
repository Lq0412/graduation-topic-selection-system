package cn.com.edtechhub.worktopicselection.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 教师选题组额度批量查询请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class TeacherGroupsBatchRequest implements Serializable {

    /**
     * 教师账号列表
     */
    private List<String> teacherAccounts;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
