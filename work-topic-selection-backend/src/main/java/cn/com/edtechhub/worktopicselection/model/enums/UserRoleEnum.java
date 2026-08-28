package cn.com.edtechhub.worktopicselection.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Getter
public enum UserRoleEnum {

    /**
     * 封号角色枚举实例
     */
    BAN_ROLE(-1, "ban"),

    /**
     * 学生角色枚举实例
     */
    STUDENT(0, "student"),

    /**
     * 教师角色枚举实例
     */
    TEACHER(1, "teacher"),

    /**
     * 主任角色枚举实例
     */
    DEPT(2, "dept"),

    /**
     * 系统角色枚举实例
     */
    ADMIN(3, "admin"),

    ;

    /**
     * 码值
     */
    private final int code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 内部角色构造方法
     */
    UserRoleEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据码值获取枚举
     */
    public static UserRoleEnum getEnums(int code) {
        if (ObjUtil.isEmpty(code)) {
            return null;
        }
        for (UserRoleEnum enumItem : UserRoleEnum.values()) {
            if (enumItem.getCode() == code) {
                return enumItem;
            }
        }
        return null;
    }

}
