package cn.com.edtechhub.worktopicselection.model.dto.dept;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class DeptAddRequest implements Serializable {

    /**
     * 系部名
     */
    private String deptName;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}