package cn.com.edtechhub.worktopicselection.service;

import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeacherGroupService {
    @Resource
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> groups(String account) {
        return jdbcTemplate.queryForList("SELECT q.groupName, q.maxTopics, "
                + "q.maxTopics - (SELECT COUNT(*) FROM topic t WHERE t.teacherAccount=q.teacherAccount "
                + "AND t.topicGroup=q.groupName AND t.isDelete=0) AS remaining "
                + "FROM teacher_group_quota q WHERE q.teacherAccount=? ORDER BY q.groupName", account);
    }

    /**
     * 批量查询多名教师的选题组额度, 供管理员/系部主任在教师列表上直接查看
     */
    public Map<String, List<Map<String, Object>>> groupsBatch(List<String> accounts) {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        if (accounts == null) {
            return result;
        }
        List<String> distinctAccounts = accounts.stream()
                .filter(account -> account != null && !account.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
        for (String account : distinctAccounts) {
            result.put(account, new ArrayList<>());
        }
        if (distinctAccounts.isEmpty()) {
            return result;
        }
        String placeholders = distinctAccounts.stream().map(item -> "?").collect(Collectors.joining(","));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT q.teacherAccount AS teacherAccount, q.groupName AS groupName, q.maxTopics AS maxTopics, "
                        + "q.maxTopics - (SELECT COUNT(*) FROM topic t WHERE t.teacherAccount=q.teacherAccount "
                        + "AND t.topicGroup=q.groupName AND t.isDelete=0) AS remaining "
                        + "FROM teacher_group_quota q WHERE q.teacherAccount IN (" + placeholders + ") "
                        + "ORDER BY q.teacherAccount, q.groupName",
                distinctAccounts.toArray());
        for (Map<String, Object> row : rows) {
            Object accountValue = row.get("teacherAccount");
            if (accountValue == null) {
                continue;
            }
            String account = String.valueOf(accountValue);
            result.computeIfAbsent(account, key -> new ArrayList<>()).add(row);
        }
        return result;
    }

    /**
     * 查询系统内现有选题组名称, 供配置专业选题组等下拉场景使用
     * <p>
     * 取 project 与 teacher_group_quota 两表已有组名的并集, 避免下拉选项被写死而与实际数据脱节。
     */
    public List<String> allGroups() {
        return jdbcTemplate.queryForList(
                "SELECT groupName FROM ("
                        + "SELECT DISTINCT groupName FROM project WHERE groupName IS NOT NULL AND groupName<>'' "
                        + "UNION SELECT DISTINCT groupName FROM teacher_group_quota WHERE groupName IS NOT NULL AND groupName<>''"
                        + ") g ORDER BY groupName", String.class);
    }

    // The caller holds the teacher row lock, shared with add/delete/update topic.
    public void validate(String account, String group, Long excludedTopicId) {
        List<Integer> limits = jdbcTemplate.queryForList(
                "SELECT maxTopics FROM teacher_group_quota WHERE teacherAccount=? AND groupName=?", Integer.class, account, group);
        ThrowUtils.throwIf(limits.size() != 1, CodeBindMessageEnums.NO_AUTH_ERROR, "请选择当前教师所属的选题组");
        Long used = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM topic WHERE teacherAccount=? AND topicGroup=? "
                + "AND isDelete=0 AND (? IS NULL OR id<>?)", Long.class, account, group, excludedTopicId, excludedTopicId);
        ThrowUtils.throwIf(used >= limits.get(0), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该组选题额度已用完");
    }
}
