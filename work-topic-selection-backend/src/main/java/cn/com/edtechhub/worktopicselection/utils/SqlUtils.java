package cn.com.edtechhub.worktopicselection.utils;

import cn.com.edtechhub.worktopicselection.constant.CommonConstant;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * SQL 工具
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
public class SqlUtils {

    private static final Set<String> USER_SORT_FIELDS = fields(
            "id", "userAccount", "userName", "createTime", "updateTime",
            "userRole", "dept", "status", "project", "topicAmount"
    );

    private static final Set<String> TOPIC_SORT_FIELDS = fields(
            "id", "topic", "type", "teacherName", "deptName", "deptTeacher",
            "createTime", "updateTime", "surplusQuantity", "startTime", "endTime",
            "status", "selectAmount"
    );

    private static final Set<String> PROJECT_SORT_FIELDS = fields(
            "id", "projectName", "deptName", "createTime", "updateTime"
    );

    private static final Set<String> DEPT_SORT_FIELDS = fields(
            "id", "deptName", "createTime", "updateTime"
    );

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     */
    public static boolean validSortField(String sortField) {
        return validUserSortField(sortField);
    }

    public static boolean validUserSortField(String sortField) {
        return USER_SORT_FIELDS.contains(sortField);
    }

    public static boolean validTopicSortField(String sortField) {
        return TOPIC_SORT_FIELDS.contains(sortField);
    }

    public static boolean validProjectSortField(String sortField) {
        return PROJECT_SORT_FIELDS.contains(sortField);
    }

    public static boolean validDeptSortField(String sortField) {
        return DEPT_SORT_FIELDS.contains(sortField);
    }

    public static boolean validSortOrder(String sortOrder) {
        return CommonConstant.SORT_ORDER_ASC.equals(sortOrder)
                || CommonConstant.SORT_ORDER_DESC.equals(sortOrder);
    }

    private static Set<String> fields(String... fields) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(fields)));
    }

}
