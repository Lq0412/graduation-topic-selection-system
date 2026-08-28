package cn.com.edtechhub.worktopicselection.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 学生专题选择状态枚举体
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Getter
public enum StudentTopicSelectionStatusEnum {

    UN_PRESELECT(-1, "取消预先选择"),

    EN_PRESELECT(0, "确认预先选择"),

    UN_SELECT(1, "取消确认选择"),

    EN_SELECT(2, "确定确认选择"),

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
     * 内部构造方法
     */
    StudentTopicSelectionStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据码值获取枚举
     */
    public static StudentTopicSelectionStatusEnum getEnums(int code) { // TODO: 等待改正放回值为枚举
        if (ObjUtil.isEmpty(code)) {
            return null;
        }
        for (StudentTopicSelectionStatusEnum enumItem : StudentTopicSelectionStatusEnum.values()) {
            if (enumItem.getCode() == code) {
                return enumItem;
            }
        }
        return null;
    }

}
