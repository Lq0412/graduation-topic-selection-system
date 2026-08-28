package cn.com.edtechhub.worktopicselection.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户名字脱敏类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Data
public class UserNameVO implements Serializable {


    /**
     * 用户名
     */
    private String userName;


    private static final long serialVersionUID = 1L;
}