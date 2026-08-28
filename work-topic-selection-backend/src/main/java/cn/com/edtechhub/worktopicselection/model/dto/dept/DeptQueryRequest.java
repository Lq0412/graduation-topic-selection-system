package cn.com.edtechhub.worktopicselection.model.dto.dept;

import cn.com.edtechhub.worktopicselection.model.dto.PageRequest;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeptQueryRequest extends PageRequest implements Serializable {

    private String deptName;

    /// 序列化字段 ///
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}