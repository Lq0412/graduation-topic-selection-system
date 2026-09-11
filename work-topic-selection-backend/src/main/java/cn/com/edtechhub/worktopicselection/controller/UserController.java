package cn.com.edtechhub.worktopicselection.controller;

import cn.com.edtechhub.worktopicselection.constant.CommonConstant;
import cn.com.edtechhub.worktopicselection.constant.TopicConstant;
import cn.com.edtechhub.worktopicselection.constant.UserConstant;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.manager.ai.AIManager;
import cn.com.edtechhub.worktopicselection.manager.ai.AIResult;
import cn.com.edtechhub.worktopicselection.manager.redis.RedisManager;
import cn.com.edtechhub.worktopicselection.manager.sentine.SentineManager;
import cn.com.edtechhub.worktopicselection.mapper.StudentTopicSelectionMapper;
import cn.com.edtechhub.worktopicselection.mapper.TopicMapper;
import cn.com.edtechhub.worktopicselection.mapper.UserMapper;
import cn.com.edtechhub.worktopicselection.model.dto.dept.DeleteDeptRequest;
import cn.com.edtechhub.worktopicselection.model.dto.dept.DeptAddRequest;
import cn.com.edtechhub.worktopicselection.model.dto.dept.DeptQueryRequest;
import cn.com.edtechhub.worktopicselection.model.dto.dept.SetDeptConfigRequest;
import cn.com.edtechhub.worktopicselection.model.dto.project.DeleteProjectRequest;
import cn.com.edtechhub.worktopicselection.model.dto.project.ProjectAddRequest;
import cn.com.edtechhub.worktopicselection.model.dto.project.ProjectQueryRequest;
import cn.com.edtechhub.worktopicselection.model.dto.project.ProjectGroupUpdateRequest;
import cn.com.edtechhub.worktopicselection.model.dto.schedule.SetTimeRequest;
import cn.com.edtechhub.worktopicselection.model.dto.schedule.UnSetTimeRequest;
import cn.com.edtechhub.worktopicselection.model.dto.studentTopicSelection.SelectStudentRequest;
import cn.com.edtechhub.worktopicselection.model.dto.studentTopicSelection.SelectTopicByIdRequest;
import cn.com.edtechhub.worktopicselection.model.dto.topic.*;
import cn.com.edtechhub.worktopicselection.model.dto.user.*;
import cn.com.edtechhub.worktopicselection.model.entity.*;
import cn.com.edtechhub.worktopicselection.model.enums.StudentTopicSelectionStatusEnum;
import cn.com.edtechhub.worktopicselection.model.enums.TopicStatusEnum;
import cn.com.edtechhub.worktopicselection.model.enums.UserRoleEnum;
import cn.com.edtechhub.worktopicselection.model.vo.*;
import cn.com.edtechhub.worktopicselection.response.BaseResponse;
import cn.com.edtechhub.worktopicselection.response.TheResult;
import cn.com.edtechhub.worktopicselection.service.*;
import cn.com.edtechhub.worktopicselection.utils.DeviceUtils;
import cn.com.edtechhub.worktopicselection.utils.IpUtils;
import cn.com.edtechhub.worktopicselection.utils.SqlUtils;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户控制层
 * TODO: 管理端强制退选清除功能
 * TODO：编写完整的单元测试
 * TODO: 可以添加一个通知所有在线用户的功能，并且支持多线程发送（就是线程不要设置太多）
 * TODO: 添加 AI 智能体阅读功能, 用于辅助教师和学生查重和寻找题目
 * TODO: 修复批量导入乱码功能
 * TODO: 修复按照当前逻辑修复选题使用教师名字的问题
 * TODO: 重构 Service 层, 该迁移就迁移
 * TODO: 重构接口层, 提高可维护性, 按道理来说, 切割接口层的影响不大, 有 IDEA 兜底
 * TODO：发现了一个小 bug 无法直接创建管理员
 * TODO：提供一个想法，考虑加一个方便管理员进行测试的一个关闭限流开关，并且可以考虑做一个网站管理配置，同时只能提供给超级管理员
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String RESET_CODE_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    private static final String CAPTCHA_ALPHABET = "23456789";

    /** 固定的无意义 BCrypt 散列，用于让不存在账号与错误密码走相同的校验成本。 */
    private static final String DUMMY_PASSWORD_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private static final String PASSWORD_RESET_CODE_PREFIX = "password-reset:";

    private static final String EMAIL_CAPTCHA_PREFIX = "email-captcha:";

    private static final String PASSWORD_RESET_RATE_LIMIT_PREFIX = "password-reset-rate:";

    private static final String EMAIL_CAPTCHA_RATE_LIMIT_PREFIX = "email-captcha-rate:";

    private static final String LOGIN_ACCOUNT_RATE_LIMIT_PREFIX = "login-account-rate:";

    private static final String LOGIN_IP_RATE_LIMIT_PREFIX = "login-ip-rate:";

    private static final String MAIL_IP_RATE_LIMIT_PREFIX = "mail-ip-rate:";

    private static final String MAIL_GLOBAL_RATE_LIMIT_KEY = "mail-global-rate";

    private static final String AI_REVIEW_RATE_LIMIT_PREFIX = "ai-review-rate:";

    private static final long ONE_TIME_CODE_TTL_SECONDS = 2 * 60;

    private static final long MAIL_RATE_LIMIT_WINDOW_SECONDS = 60;

    private static final long LOGIN_RATE_LIMIT_WINDOW_SECONDS = 5 * 60;

    private static final long MAIL_IP_RATE_LIMIT_WINDOW_SECONDS = 10 * 60;

    private static final long MAIL_GLOBAL_RATE_LIMIT_WINDOW_SECONDS = 60 * 60;

    private static final long VERIFIED_EMAIL_TTL_MILLIS = 5 * 60 * 1000L;

    /**
     * 注入 SentineManager 依赖
     */
    @Resource
    SentineManager sentineManager;

    /**
     * 注入事务管理依赖
     */
    @Resource
    TransactionTemplate transactionTemplate;

    /**
     * 注入 Redis 管理依赖
     */
    @Resource
    RedisManager redisManager;

    /**
     * 注入 AI 管理依赖
     */
    @Resource
    private AIManager aiManager;

    /**
     * 注入用户服务依赖
     */
    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeacherGroupService teacherGroupService;

    /**
     * 注入系部服务依赖
     */
    @Resource
    private DeptService deptService;

    /**
     * 注入专业服务依赖
     */
    @Resource
    private ProjectService projectService;

    /**
     * 注入选题服务依赖
     */
    @Resource
    private TopicService topicService;

    @Resource
    private TopicMapper topicMapper;

    /**
     * 注入学生选题关联服务依赖
     */
    @Resource
    private StudentTopicSelectionService studentTopicSelectionService;

    @Resource
    private StudentTopicSelectionMapper studentTopicSelectionMapper;

    /**
     * 注入邮箱服务依赖
     */
    @Resource
    private MailService mailService;

    /**
     * 引入开关服务依赖
     */
    @Resource
    private SwitchService switchService;

    /// 测试相关接口 ///
    @GetMapping("/test")
    public BaseResponse<String> test() {
        return TheResult.notyet();
    }

    /// 用户相关接口 ///

    /**
     * 创建用户接口
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "缺少用户的学号/工号");
        final String normalizedUserAccount = userAccount.trim();
        ThrowUtils.throwIf(normalizedUserAccount.length() > UserConstant.MAX_USER_ACCOUNT_LENGTH, CodeBindMessageEnums.PARAMS_ERROR, "用户账号不能超过 128 个字符");

        String userName = request.getUserName();
        ThrowUtils.throwIf(StringUtils.isBlank(userName), CodeBindMessageEnums.PARAMS_ERROR, "缺少用户名字");

        User oldUser = userService.userIsExist(normalizedUserAccount, userName);
        ThrowUtils.throwIf(oldUser != null, CodeBindMessageEnums.PARAMS_ERROR, "该学号/工号对应的用户已经存在, 请不要重复添加, 如果需要更改用户信息可以先删除用户再重新添加");

        Integer userRole = request.getUserRole();
        ThrowUtils.throwIf(userRole == null, CodeBindMessageEnums.PARAMS_ERROR, "缺少用户角色");
        assert userRole != null;

        UserRoleEnum userRoleEnum = UserRoleEnum.getEnums(userRole);
        ThrowUtils.throwIf(userRoleEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "本系统不存在该用户角色");

        // 除了管理员帐号都需要系部和专业信息来注册帐号
        String userDeptName = request.getDeptName();
        if (!Objects.equals(userRoleEnum, UserRoleEnum.ADMIN)) {
            ThrowUtils.throwIf(StringUtils.isBlank(userDeptName), CodeBindMessageEnums.PARAMS_ERROR, "缺少系部名称");
        }

        // 不允许添加相同角色并且名字相同的用户
        User aUser = userService.getOne(new QueryWrapper<User>()
                .eq("userName", userName)
                .eq("userRole", userRole)
        );
        ThrowUtils.throwIf(aUser != null, CodeBindMessageEnums.PARAMS_ERROR, "不允许添加相同角色的同名用户, 请不要重复添加, 请加上数字后缀避免相同");

        // 如果有选择专业则必须选择系部所属的专业
        String userProject = request.getProject();
        if (StringUtils.isNotBlank(userProject)) {
            Project project = projectService.getOne(new QueryWrapper<Project>().eq("projectName", userProject));
            ThrowUtils.throwIf(project == null, CodeBindMessageEnums.PARAMS_ERROR, "所选专业不存在");
            assert project != null;
            ThrowUtils.throwIf(!project.getDeptName().equals(userDeptName), CodeBindMessageEnums.PARAMS_ERROR, "[" + project.getProjectName() + "] 专业属于 [" + project.getDeptName() + "] 系部, 请正确选择系部和专业");
        }

        // 如果是添加学生则需要检查是否设置了系部和专业
        if (userRoleEnum == UserRoleEnum.STUDENT) {
            ThrowUtils.throwIf(StringUtils.isBlank(userDeptName), CodeBindMessageEnums.PARAMS_ERROR, "缺少系部名称");
            ThrowUtils.throwIf(StringUtils.isBlank(userProject), CodeBindMessageEnums.PARAMS_ERROR, "缺少专业名称");
        }
        // 如果是添加主任或教师则需要检查是否设置了系部和专业
        else if (userRoleEnum == UserRoleEnum.DEPT || userRoleEnum == UserRoleEnum.TEACHER) {
            ThrowUtils.throwIf(StringUtils.isBlank(userDeptName), CodeBindMessageEnums.PARAMS_ERROR, "缺少系部名称");
        } else if (userRoleEnum == UserRoleEnum.ADMIN) {
            log.info("添加管理员");
        }
        // 兜底情况
        else {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.SYSTEM_ERROR, "系统发生未知情况，请联系系统管理员");
        }

        // 创建新的用户实例。临时密码只在本次响应中展示，服务端仅保存 BCrypt 散列。
        String temporaryPassword = userService.generateTemporaryPassword();
        return transactionTemplate.execute(transactionStatus -> {
            User user = new User();
            BeanUtils.copyProperties(request, user); // 数据库中的 userAccount 是主键
            user.setUserAccount(normalizedUserAccount);
            user.setDept(userDeptName);
            user.setProject(userProject);
            user.setUserRole(userRole);
            user.setStatus(null);
            user.setUserPassword(userService.encodePassword(temporaryPassword));

            boolean result = userService.save(user); // TODO：这里有个逻辑字段重复删除失败的问题以后解决
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "添加新的用户失败");
            return new BaseResponse<>(
                    CodeBindMessageEnums.SUCCESS.getCode(),
                    "成功；临时密码（仅显示一次）：" + temporaryPassword,
                    user.getId()
            );
        });
    }

    /**
     * 删除用户接口
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "指定删除的用户账号不能为空");

        User user = userService.userIsExist(userAccount);
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.PARAMS_ERROR, "用户不存在无需删除");
        assert user != null;

        // 删除用户
        BaseResponse<Boolean> response = transactionTemplate.execute(transactionStatus -> {
            // 不允许删除超级管理员
            Long id = user.getId();
            ThrowUtils.throwIf(id == 1L, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "无法删除超级管理员");

            // 如果删除的是教师, 则需要把该教师关联的选题记录删除, 同时清空选题关联记录(无论是否预选)
            if (userService.userIsTeacher(user)) {
                // 先找出该教师的所有关联题目的 id
                List<Topic> topicList = topicService.list(
                        new QueryWrapper<Topic>().eq("teacherAccount", user.getUserAccount())
                );
                List<Long> topicIds = topicList
                        .stream()
                        .map(Topic::getId)
                        .collect(Collectors.toList());

                // 如果该教师确实有题目
                if (!topicIds.isEmpty()) {
                    // 删除关联的学生选题记录
                    studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().in("topicId", topicIds));
                    // 同时删除这些题目
                    topicService.removeByIds(topicIds);
                }
            }

            // 如果删除的是学生, 则需要清空选题关联记录(无论是否预选)
            if (userService.userIsStudent(user)) {
                User lockedStudent = userMapper.selectByIdForUpdate(user.getId());
                ThrowUtils.throwIf(lockedStudent == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "用户不存在无需删除");

                List<StudentTopicSelection> selections = studentTopicSelectionService.list(
                        new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount)
                );
                if (!selections.isEmpty()) {
                    List<Long> topicIds = selections
                            .stream()
                            .map(StudentTopicSelection::getTopicId)
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());

                    Map<Long, Topic> lockedTopics = new HashMap<>();
                    for (Long topicId : topicIds) {
                        Topic topic = topicMapper.selectByIdForUpdate(topicId);
                        if (topic != null) {
                            lockedTopics.put(topicId, topic);
                        }
                    }

                    List<StudentTopicSelection> lockedSelections = studentTopicSelectionMapper.selectByUserForUpdate(userAccount);
                    for (StudentTopicSelection selection : lockedSelections) {
                        Topic topic = lockedTopics.get(selection.getTopicId());
                        if (topic == null || !isActiveSelection(selection)) {
                            continue;
                        }
                        restoreTopicCounters(topic, selection);
                        boolean updated = topicService.updateById(topic);
                        ThrowUtils.throwIf(!updated, CodeBindMessageEnums.OPERATION_ERROR, "无法恢复题目余量");
                    }

                }
            }

            // 删除用户
            boolean result = userService.removeById(user.getId());
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.SYSTEM_ERROR, "删除用户失败");

            // 删除用户所选的选题关联记录
            studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount));
            return TheResult.success(CodeBindMessageEnums.SUCCESS, result);
        });
        StpUtil.logout(user.getId());
        return response;
    }

    /**
     * 更新用户接口
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long id = request.getId();
        ThrowUtils.throwIf(id == null || id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "用户标识不合法, 必须为正整数");

        User oldUser = userService.getById(id);
        ThrowUtils.throwIf(oldUser == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "用户不存在");

        Integer newRole = request.getUserRole();
        if (newRole != null) {
            ThrowUtils.throwIf(UserRoleEnum.getEnums(newRole) == null, CodeBindMessageEnums.PARAMS_ERROR, "该用户角色不存在");
            ThrowUtils.throwIf(
                    (Objects.equals(newRole, UserRoleEnum.DEPT.getCode())
                            || Objects.equals(newRole, UserRoleEnum.TEACHER.getCode()))
                            && StringUtils.isBlank(oldUser.getDept()),
                    CodeBindMessageEnums.PARAMS_ERROR,
                    "专业负责人或教师账号必须先配置所属系部"
            );
            ThrowUtils.throwIf(
                    Objects.equals(newRole, UserRoleEnum.STUDENT.getCode())
                            && (StringUtils.isBlank(oldUser.getDept()) || StringUtils.isBlank(oldUser.getProject())),
                    CodeBindMessageEnums.PARAMS_ERROR,
                    "学生账号必须先配置所属系部和专业"
            );
        }
        boolean roleChanged = newRole != null && !Objects.equals(oldUser.getUserRole(), newRole);

        // 创建更新后的新用户实例
        BaseResponse<Boolean> response = transactionTemplate.execute(transactionStatus -> {
            User user = new User();
            BeanUtils.copyProperties(request, user);

            boolean result = userService.updateById(user);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "更新失败");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        });
        if (roleChanged) {
            StpUtil.logout(id);
        }
        return response;
    }

    /**
     * 获取当前登录用户数据
     */
    @SaCheckLogin
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 获取当前登录用户
        User user = userService.userGetCurrentLoginUser();
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userService.getLoginUserVO(user));
    }

    /**
     * 获取用户分页数据
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin", "teacher"}, mode = SaMode.OR)
    @PostMapping("/get/user/page")
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;
        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1 || size > 100, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须在 1 到 100 之间");

        User loginUser = userService.userGetCurrentLoginUser();
        Integer requestedRole = request.getUserRole();
        if (!userService.userIsAdmin(loginUser)) {
            requireDepartment(loginUser);
            ThrowUtils.throwIf(
                    !userService.userIsTeacher(loginUser)
                            || !Objects.equals(requestedRole, UserRoleEnum.STUDENT.getCode()),
                    CodeBindMessageEnums.NO_AUTH_ERROR,
                    "教师只能查看本系学生列表"
            );
        }

        // 获取搜索条件
        QueryWrapper<User> queryWrapper = userService.getQueryWrapper(request);
        if (!userService.userIsAdmin(loginUser)) {
            queryWrapper.eq("dept", loginUser.getDept());
        }

        // 获取用户数据
        Page<User> userPage = userService.page(new Page<>(current, size), queryWrapper);

        return TheResult.success(CodeBindMessageEnums.SUCCESS, userPage);
    }

    /**
     * 获取所有教师的脱敏列表数据接口 (教师自己查主任只能获得同系部的主任)
     */
    @SaCheckLogin
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/get/teacher")
    public BaseResponse<List<TeacherVO>> getTeacher(@RequestBody TeacherQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Integer userRole = request.getUserRole();
        ThrowUtils.throwIf(userRole == null, CodeBindMessageEnums.PARAMS_ERROR, "用户角色不能为空");

        UserRoleEnum userRoleEnum = UserRoleEnum.getEnums(userRole);
        ThrowUtils.throwIf(userRoleEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "该用户角色不存在");
        assert userRoleEnum != null;

        User loginUser = userService.userGetCurrentLoginUser();
        requireDepartment(loginUser);

        // 获取所有的教师数据（如果当前登陆用户是教师且查询的是主任就只能查询和自己同系部的主任）
        List<User> userList = userService.list(
                new QueryWrapper<User>()
                        .eq("userRole", userRoleEnum.getCode())
                        .eq(loginUser.getUserRole().equals(UserRoleEnum.TEACHER.getCode()), "dept", loginUser.getDept())
        );
        List<TeacherVO> teacherVOList = new ArrayList<>();
        for (User user : userList) {
            TeacherVO teacherVO = new TeacherVO();
            final String userName = user.getUserName();
            teacherVO.setLabel(userName);
            teacherVO.setValue(userName);
            teacherVOList.add(teacherVO);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, teacherVOList);
    }

    /**
     * 根据 id 获取用户数据
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "用户标识必须是正整数");

        // 返回用户信息
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "不存在该用户");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, user);
    }

    /**
     * 根据 id 获取用户包装数据 (但是获取的是脱敏后的数据)
     */
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "用户标识必须是正整数");

        // 获取用户数据
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();

        // 获取脱敏后的数据
        UserVO userVO = userService.getUserVO(user);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userVO);
    }

    /// 认证相关接口 ///

    /**
     * 用户登入
     */
    @SaIgnore
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest request, HttpServletRequest httpServletrequest) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "缺少登陆时所需要的账号");
        userAccount = userAccount.trim();
        ThrowUtils.throwIf(userAccount.length() > UserConstant.MAX_USER_ACCOUNT_LENGTH, CodeBindMessageEnums.PARAMS_ERROR, "账号格式不正确");

        String userPassword = request.getUserPassword();
        ThrowUtils.throwIf(StringUtils.isBlank(userPassword), CodeBindMessageEnums.PARAMS_ERROR, "缺少登陆时所需要的密码");

        User user = userService.userIsExist(userAccount);
        String rateLimitAccount = canonicalAccountForRateLimit(userAccount, user);
        enforceLoginRateLimit(rateLimitAccount, httpServletrequest);
        boolean passwordMatches;
        if (user == null) {
            userService.matchesPassword(userPassword, DUMMY_PASSWORD_HASH);
            passwordMatches = false;
        } else {
            passwordMatches = userService.matchesPassword(userPassword, user.getUserPassword());
        }
        if (!passwordMatches) {
            ThrowUtils.throwIf(true, CodeBindMessageEnums.PARAMS_ERROR, "账号或密码错误");
        }
        assert user != null;
        UserRoleEnum loginRole = UserRoleEnum.getEnums(user.getUserRole());
        ThrowUtils.throwIf(
                loginRole == null || UserRoleEnum.BAN_ROLE.equals(loginRole),
                CodeBindMessageEnums.NO_AUTH_ERROR,
                "账号已被禁用，请联系系统管理员"
        );
        ThrowUtils.throwIf(StringUtils.isBlank(user.getStatus()), CodeBindMessageEnums.USER_INIT_PASSWD, "您正在使用初始临时密码, 请修改密码后再登陆");

        // 旧版 MD5 用户在首次成功校验时原地升级，后续只使用 BCrypt。
        if (userService.needsPasswordUpgrade(user.getUserPassword())) {
            String oldPasswordHash = user.getUserPassword();
            String upgradedPassword = userService.encodePasswordForMigration(userPassword);
            boolean upgraded = userService.update(
                    new UpdateWrapper<User>()
                            .eq("id", user.getId())
                            .eq("userPassword", oldPasswordHash)
                            .set("userPassword", upgradedPassword)
            );
            ThrowUtils.throwIf(!upgraded, CodeBindMessageEnums.OPERATION_ERROR, "升级密码安全格式失败，请稍后重试");
            user.setUserPassword(upgradedPassword);
        }

        // 用户登陆
        String device = DeviceUtils.getRequestDevice(httpServletrequest); // 登陆设备
        StpUtil.login(user.getId(), device); // 开始登录
        StpUtil.getSession().set(UserConstant.USER_LOGIN_STATE, user); // 把用户的信息存储到 Sa-Token 的会话中, 这样后续的用权限判断就不需要一直查询 SQL 才能得到, 缺点是更新权限的时候需要把用户踢下线否则会话无法更新
        releaseLoginAttempt(rateLimitAccount, httpServletrequest);

        // 数据脱敏
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, loginUserVO);
    }

    /**
     * 用户注销
     */
    @SaCheckLogin
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        // 注销用户
        StpUtil.logout(); // 默认所有设备都被登出
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    /**
     * 用户切换登陆
     */
    @SaCheckLogin
    @SaCheckRole(value = {"dept", "teacher"}, mode = SaMode.OR)
    @PostMapping("/toggle/login")
    public BaseResponse<LoginUserVO> userToggleLogin(@RequestBody UserToggleRequest request, HttpServletRequest httpServletrequest) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;
        Integer userRole = request.getUserRole();
        ThrowUtils.throwIf(userRole == null, CodeBindMessageEnums.PARAMS_ERROR, "缺少切换所需要的角色");
        assert userRole != null;
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnums(userRole);
        ThrowUtils.throwIf(userRoleEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "想要切换帐号的角色不存在");
        assert userRoleEnum != null;
        User loginUser = userService.userGetCurrentLoginUser();
        ThrowUtils.throwIf(loginUser.getUserRole().equals(userRole), CodeBindMessageEnums.PARAMS_ERROR, "当前用户已经是该角色了, 无需切换帐号");
        ThrowUtils.throwIf(
                !isAllowedRoleToggle(loginUser.getUserRole(), userRole),
                CodeBindMessageEnums.NO_AUTH_ERROR,
                "只允许教师帐号和专业负责人帐号互相切换"
        );

        // 当前用户必须绑定邮箱
        ThrowUtils.throwIf(StringUtils.isBlank(loginUser.getEmail()), CodeBindMessageEnums.USER_INIT_PASSWD, "本帐号必须先绑定邮箱");

        // BCrypt 每个密码都有独立随机盐，不能再通过散列相等来关联帐号；改用已绑定邮箱和身份字段。
        List<User> userList = userService.list(
                new QueryWrapper<User>()
                        .ne("id", loginUser.getId())
                        .eq("status", "老用户")
                        .eq("userName", loginUser.getUserName())
                        .eq("dept", loginUser.getDept())
                        .eq("email", loginUser.getEmail())
                        .eq("userRole", userRole)
        );
        ThrowUtils.throwIf(userList.size() != 1, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "无法唯一确定要切换的帐号, 请联系管理员检查同名、同系、同邮箱的帐号数据");

        // 用户切换登陆
        StpUtil.logout(loginUser.getId());
        User newUser = userList.get(0);
        String device = DeviceUtils.getRequestDevice(httpServletrequest);
        StpUtil.login(newUser.getId(), device);
        StpUtil.getSession().set(UserConstant.USER_LOGIN_STATE, newUser); // 把用户的信息存储到 Sa-Token 的会话中, 这样后续的用权限判断就不需要一直查询 SQL 才能得到, 缺点是更新权限的时候需要把用户踢下线否则会话无法更新

        // 数据脱敏
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(newUser, loginUserVO);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, loginUserVO);
    }

    static boolean isAllowedRoleToggle(Integer currentRole, Integer targetRole) {
        return (Objects.equals(currentRole, UserRoleEnum.TEACHER.getCode())
                && Objects.equals(targetRole, UserRoleEnum.DEPT.getCode()))
                || (Objects.equals(currentRole, UserRoleEnum.DEPT.getCode())
                && Objects.equals(targetRole, UserRoleEnum.TEACHER.getCode()));
    }

    /**
     * 重置用户密码
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/reset/password")
    public BaseResponse<String> resetPassword(@RequestBody ResetPasswordRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userName = request.getUserName();
        ThrowUtils.throwIf(StringUtils.isBlank(userName), CodeBindMessageEnums.PARAMS_ERROR, "用户名不能为空");

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "用户账号不能为空");

        User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount).eq("userName", userName));
        ThrowUtils.throwIf(user == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "该用户不存在, 无需重置密码");
        assert user != null;

        // 禁止对超级管理员进行操作
        ThrowUtils.throwIf(user.getId() == 1L, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "禁止对超级管理员进行该操作");

        // 获取新的随机临时密码，仅在本次响应中展示。
        String temporaryPassword = userService.generateTemporaryPassword();
        BaseResponse<String> response = transactionTemplate.execute(transactionStatus -> {
            user.setUserPassword(userService.encodePassword(temporaryPassword));
            user.setStatus(""); // 状态置新, 以方便后续强制要求用户重新登陆
            final boolean result = userService.updateById(user);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "修改用户密码失败");
            return new BaseResponse<>(
                    CodeBindMessageEnums.SUCCESS.getCode(),
                    "成功；临时密码（仅显示一次）：" + temporaryPassword,
                    user.getUserAccount()
            );
        });
        StpUtil.logout(user.getId());
        return response;
    }

    /**
     * 用户自己手动修改密码
     */
    @SaIgnore
    @PostMapping("/updata/password")
    public BaseResponse<Long> userUpdatePassword(@RequestBody UserUpdatePassword request, HttpServletRequest httpServletRequest) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "用户学号/工号不能为空");
        userAccount = userAccount.trim();
        ThrowUtils.throwIf(userAccount.length() > UserConstant.MAX_USER_ACCOUNT_LENGTH, CodeBindMessageEnums.PARAMS_ERROR, "用户账号不能超过 128 个字符");

        String code = request.getCode();
        String userPassword = request.getUserPassword();
        ThrowUtils.throwIf(StringUtils.isBlank(code) && StringUtils.isBlank(userPassword), CodeBindMessageEnums.PARAMS_ERROR, "用户旧密码或验证码不能为空");

        String updatePassword = request.getUpdatePassword();
        ThrowUtils.throwIf(StringUtils.isBlank(updatePassword), CodeBindMessageEnums.PARAMS_ERROR, "新密码不能为空");
        ThrowUtils.throwIf(!userService.isPasswordValid(updatePassword), CodeBindMessageEnums.PARAMS_ERROR, "新密码长度必须为 8 到 72 个 UTF-8 字节");

        // 必须通过学号或工号以及密码验证后才能修改密码
        User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));

        if (StringUtils.isNotBlank(code)) {
            // 如果使用验证码重置
            String canonicalUserAccount = user == null ? userAccount : user.getUserAccount();
            long consumeResult = redisManager.consumeValue(PASSWORD_RESET_CODE_PREFIX + canonicalUserAccount, code);
            ThrowUtils.throwIf(consumeResult == 0, CodeBindMessageEnums.NOT_FOUND_ERROR, "临时密码已过期, 请重新获取临时密码");
            ThrowUtils.throwIf(consumeResult != 1, CodeBindMessageEnums.PARAMS_ERROR, "您的临时密码错误，请重新获取");
            ThrowUtils.throwIf(user == null, CodeBindMessageEnums.PARAMS_ERROR, "账号或临时密码错误");
        } else {
            // 如果使用旧密码重置
            String rateLimitAccount = canonicalAccountForRateLimit(userAccount, user);
            enforceLoginRateLimit(rateLimitAccount, httpServletRequest);
            boolean passwordMatches;
            if (user == null) {
                userService.matchesPassword(userPassword, DUMMY_PASSWORD_HASH);
                passwordMatches = false;
            } else {
                passwordMatches = userService.matchesPassword(userPassword, user.getUserPassword());
            }
            if (!passwordMatches) {
                ThrowUtils.throwIf(true, CodeBindMessageEnums.PARAMS_ERROR, "账号或旧密码错误");
            }
            assert user != null;
            releaseLoginAttempt(rateLimitAccount, httpServletRequest);
            ThrowUtils.throwIf(
                    StringUtils.isBlank(user.getStatus()) && userService.needsPasswordUpgrade(user.getUserPassword()),
                    CodeBindMessageEnums.USER_INIT_PASSWD,
                    "旧版初始账号需要管理员先重置为随机临时密码"
            );
        }
        assert user != null;

        // 如果邮箱不为空并且用户还尚未注册绑定邮箱则需要绑定邮箱
        String email = normalizeEmail(request.getEmail());
        if (StringUtils.isNotBlank(email)) {
            if (StringUtils.isBlank(user.getEmail())) {
                ThrowUtils.throwIf(!consumeVerifiedEmail(httpServletRequest, email), CodeBindMessageEnums.PARAMS_ERROR, "邮箱验证码未校验或已过期，请重新验证");
                user.setEmail(email);
            } else {
                ThrowUtils.throwIf(!normalizeEmail(user.getEmail()).equals(email), CodeBindMessageEnums.PARAMS_ERROR, "用户已绑定其他邮箱；更换邮箱请联系系统管理员");
            }
        }

        // 更新状态避免重复修改密码
        user.setStatus("老用户");

        // 更新用户
        User finalUser = user;
        BaseResponse<Long> response = transactionTemplate.execute(transactionStatus -> {
            finalUser.setUserPassword(userService.encodePassword(updatePassword));
            boolean result = userService.updateById(finalUser);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "修改密码失败");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, finalUser.getId());
        });
        StpUtil.logout(finalUser.getId());
        return response;
    }

    /**
     * 发送临时密码
     */
    @SaIgnore
    @PostMapping("/send/code")
    public BaseResponse<String> sendCode(@RequestBody SendCodeRequest request, HttpServletRequest httpServletRequest) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "用户帐号不能为空");
        userAccount = userAccount.trim();
        ThrowUtils.throwIf(userAccount.length() > UserConstant.MAX_USER_ACCOUNT_LENGTH, CodeBindMessageEnums.PARAMS_ERROR, "用户账号不能超过 128 个字符");

        // 先查询数据库中的规范账号，避免利用数据库不区分大小写/重音的排序规则轮换限流键。
        User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
        String rateLimitAccount = canonicalAccountForRateLimit(userAccount, user);
        enforceMailRateLimit(
                PASSWORD_RESET_RATE_LIMIT_PREFIX + rateLimitAccount,
                httpServletRequest,
                "临时密码发送过于频繁，请稍后重试",
                user != null && StringUtils.isNotBlank(user.getEmail())
        );

        // 查找用户邮箱
        if (user == null || StringUtils.isBlank(user.getEmail())) {
            return TheResult.success(CodeBindMessageEnums.SUCCESS, "若账号存在且已绑定邮箱，临时密码将发送到该邮箱");
        }

        String email = normalizeEmail(user.getEmail());

        String code = generateOneTimeCode(12, RESET_CODE_ALPHABET);
        String redisKey = PASSWORD_RESET_CODE_PREFIX + user.getUserAccount();
        redisManager.setValue(redisKey, code, ONE_TIME_CODE_TTL_SECONDS);

        try {
            mailService.sendCodeMail(email, "毕业设计选题系统", code);
        } catch (Exception e) {
            redisManager.deleteKey(redisKey);
            ThrowUtils.throwIf(true, CodeBindMessageEnums.SYSTEM_ERROR, "邮件发送失败，请联系系统管理员");
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, "若账号存在且已绑定邮箱，临时密码将发送到该邮箱");
    }

    /**
     * 发送验证码
     */
    @SaIgnore
    @PostMapping("/send/captcha")
    public BaseResponse<String> sendCaptcha(@RequestBody CaptchaRequest request, HttpServletRequest httpServletRequest) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String email = normalizeEmail(request.getEmail());
        ThrowUtils.throwIf(!isValidEmail(email), CodeBindMessageEnums.PARAMS_ERROR, "需要发送验证码的邮箱格式不正确");
        enforceMailRateLimit(
                EMAIL_CAPTCHA_RATE_LIMIT_PREFIX + email,
                httpServletRequest,
                "验证码发送过于频繁，请稍后重试"
        );

        String code = generateOneTimeCode(6, CAPTCHA_ALPHABET);
        String redisKey = EMAIL_CAPTCHA_PREFIX + email;
        redisManager.setValue(redisKey, code, ONE_TIME_CODE_TTL_SECONDS);

        try {
            mailService.sendCaptchaMail(email, "毕业设计选题系统", code);
        } catch (Exception e) {
            redisManager.deleteKey(redisKey);
            ThrowUtils.throwIf(true, CodeBindMessageEnums.SYSTEM_ERROR, "邮件发送失败，请联系系统管理员");
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, "发送成功, 请在您的 QQ 邮箱中查收!");
    }

    /**
     * 校验验证码
     */
    @SaIgnore
    @PostMapping("/check/captcha")
    public BaseResponse<Boolean> checkCaptcha(@RequestBody CheckCaptchaRequest request, HttpServletRequest httpServletRequest) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String email = normalizeEmail(request.getEmail());
        ThrowUtils.throwIf(!isValidEmail(email), CodeBindMessageEnums.PARAMS_ERROR, "邮箱格式不正确, 无法确认该邮箱归您本人所属");

        String captcha = request.getCaptcha();
        ThrowUtils.throwIf(StringUtils.isBlank(captcha), CodeBindMessageEnums.PARAMS_ERROR, "没有填写校验码, 无法确认该邮箱归您本人所属");

        long consumeResult = redisManager.consumeValue(EMAIL_CAPTCHA_PREFIX + email, captcha);
        ThrowUtils.throwIf(consumeResult == 0, CodeBindMessageEnums.NOT_FOUND_ERROR, "请先点击发送验证码或验证码已过期");
        ThrowUtils.throwIf(consumeResult != 1, CodeBindMessageEnums.PARAMS_ERROR, "验证码错误，请重新获取");

        // 将验证结果绑定到当前浏览器会话；改密接口会校验邮箱并一次性消费。
        HttpSession session = httpServletRequest.getSession(true);
        synchronized (session) {
            session.setAttribute(UserConstant.VERIFIED_EMAIL_SESSION_KEY, email);
            session.setAttribute(
                    UserConstant.VERIFIED_EMAIL_EXPIRES_AT_SESSION_KEY,
                    System.currentTimeMillis() + VERIFIED_EMAIL_TTL_MILLIS
            );
        }

        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    static String canonicalAccountForRateLimit(String requestedAccount, User user) {
        String account = user != null && StringUtils.isNotBlank(user.getUserAccount())
                ? user.getUserAccount()
                : requestedAccount;
        if (account == null) {
            return "unknown";
        }
        return Normalizer.normalize(account.trim(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }

    void enforceLoginRateLimit(String userAccount, HttpServletRequest request) {
        String accountKey = LOGIN_ACCOUNT_RATE_LIMIT_PREFIX + canonicalAccountForRateLimit(userAccount, null);
        String addressKey = LOGIN_IP_RATE_LIMIT_PREFIX + getRateLimitAddress(request);
        boolean accountAllowed = redisManager.tryAcquire(
                accountKey,
                8,
                LOGIN_RATE_LIMIT_WINDOW_SECONDS
        );
        boolean addressAllowed = redisManager.tryAcquire(
                addressKey,
                300,
                LOGIN_RATE_LIMIT_WINDOW_SECONDS
        );
        if (!accountAllowed || !addressAllowed) {
            if (accountAllowed) {
                redisManager.releaseRateLimit(accountKey);
            }
            if (addressAllowed) {
                redisManager.releaseRateLimit(addressKey);
            }
        }
        ThrowUtils.throwIf(
                !accountAllowed || !addressAllowed,
                CodeBindMessageEnums.FLOW_RULES,
                "登录尝试过于频繁，请 5 分钟后重试"
        );
    }

    void releaseLoginAttempt(String userAccount, HttpServletRequest request) {
        redisManager.releaseRateLimit(
                LOGIN_ACCOUNT_RATE_LIMIT_PREFIX + canonicalAccountForRateLimit(userAccount, null)
        );
        redisManager.releaseRateLimit(
                LOGIN_IP_RATE_LIMIT_PREFIX + getRateLimitAddress(request)
        );
    }

    void enforceMailRateLimit(String targetKey, HttpServletRequest request, String message) {
        enforceMailRateLimit(targetKey, request, message, true);
    }

    void enforceMailRateLimit(String targetKey, HttpServletRequest request, String message, boolean acquireGlobal) {
        boolean targetAllowed = redisManager.tryAcquire(targetKey, 1, MAIL_RATE_LIMIT_WINDOW_SECONDS);
        ThrowUtils.throwIf(!targetAllowed, CodeBindMessageEnums.FLOW_RULES, message);

        String addressKey = MAIL_IP_RATE_LIMIT_PREFIX + getRateLimitAddress(request);
        boolean addressAllowed = redisManager.tryAcquire(
                addressKey,
                100,
                MAIL_IP_RATE_LIMIT_WINDOW_SECONDS
        );
        if (!addressAllowed) {
            redisManager.releaseRateLimit(targetKey);
            ThrowUtils.throwIf(true, CodeBindMessageEnums.FLOW_RULES, message);
        }

        if (!acquireGlobal) {
            return;
        }

        boolean globalAllowed = redisManager.tryAcquire(
                MAIL_GLOBAL_RATE_LIMIT_KEY,
                200,
                MAIL_GLOBAL_RATE_LIMIT_WINDOW_SECONDS
        );
        if (!globalAllowed) {
            redisManager.releaseRateLimit(targetKey);
            redisManager.releaseRateLimit(addressKey);
            ThrowUtils.throwIf(true, CodeBindMessageEnums.FLOW_RULES, message);
        }
    }

    private static String getRateLimitAddress(HttpServletRequest request) {
        String address = request == null ? null : request.getRemoteAddr();
        if (StringUtils.isBlank(address)) {
            return "unknown";
        }
        String normalized = address.trim().replaceAll("[^0-9A-Fa-f:.]", "_");
        return normalized.length() <= 64 ? normalized : normalized.substring(0, 64);
    }

    private static boolean isValidEmail(String email) {
        return StringUtils.isNotBlank(email)
                && email.length() <= 254
                && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private static String generateOneTimeCode(int length, String alphabet) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(alphabet.charAt(SECURE_RANDOM.nextInt(alphabet.length())));
        }
        return code.toString();
    }

    static boolean consumeVerifiedEmail(HttpServletRequest request, String email) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        synchronized (session) {
            Object verifiedEmail = session.getAttribute(UserConstant.VERIFIED_EMAIL_SESSION_KEY);
            Object expiresAt = session.getAttribute(UserConstant.VERIFIED_EMAIL_EXPIRES_AT_SESSION_KEY);
            session.removeAttribute(UserConstant.VERIFIED_EMAIL_SESSION_KEY);
            session.removeAttribute(UserConstant.VERIFIED_EMAIL_EXPIRES_AT_SESSION_KEY);
            return email.equals(verifiedEmail)
                    && expiresAt instanceof Long
                    && (Long) expiresAt >= System.currentTimeMillis();
        }
    }

    /// 系部专业相关接口 ///

    /**
     * 添加系部
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/add/dept")
    public BaseResponse<Long> addDept(@RequestBody DeptAddRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String deptName = request.getDeptName();
        ThrowUtils.throwIf(deptName == null, CodeBindMessageEnums.PARAMS_ERROR, "系部名称不能为空");

        Dept dept = deptService.getOne(new QueryWrapper<Dept>().eq("deptName", deptName));
        ThrowUtils.throwIf(dept != null, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该系部已存在, 请不要重复添加");

        // 添加新的系部
        return transactionTemplate.execute(transactionStatus -> {
            Dept newDept = new Dept();
            newDept.setDeptName(deptName);
            boolean result = deptService.save(newDept);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "无法添加新的系部");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, newDept.getId());
        });
    }

    /**
     * 添加专业
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/add/project")
    public BaseResponse<Long> addProject(@RequestBody ProjectAddRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String projectName = request.getProjectName();
        ThrowUtils.throwIf(projectName == null, CodeBindMessageEnums.PARAMS_ERROR, "专业名称不能为空");

        String deptName = request.getDeptName();
        ThrowUtils.throwIf(deptName == null, CodeBindMessageEnums.PARAMS_ERROR, "系部名称不能为空");

        Project project = projectService.getOne(new QueryWrapper<Project>().eq("projectName", projectName));
        ThrowUtils.throwIf(project != null, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该专业已存在, 请不要重复添加");

        // 添加新的专业
        return transactionTemplate.execute(transactionStatus -> {
            Project newProject = new Project();
            newProject.setProjectName(projectName);
            newProject.setDeptName(deptName);
            newProject.setGroupName(StringUtils.trimToNull(request.getGroupName()));
            boolean result = projectService.save(newProject);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "无法添加新的专业");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, newProject.getId());
        });
    }

    /**
     * 配置专业所属选题组。
     *
     * <p>一个专业只保存一个选题组；再次提交同一专业时覆盖原配置，
     * 传空组名则取消分组，便于管理员在正式开放前调整配置。</p>
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/update/project/group")
    public BaseResponse<Boolean> updateProjectGroup(@RequestBody ProjectGroupUpdateRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;
        String projectName = StringUtils.trimToNull(request.getProjectName());
        ThrowUtils.throwIf(projectName == null, CodeBindMessageEnums.PARAMS_ERROR, "专业名称不能为空");

        Project project = projectService.getOne(new QueryWrapper<Project>().eq("projectName", projectName));
        ThrowUtils.throwIf(project == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "专业不存在");

        project.setGroupName(StringUtils.trimToNull(request.getGroupName()));
        boolean updated = projectService.updateById(project);
        ThrowUtils.throwIf(!updated, CodeBindMessageEnums.OPERATION_ERROR, "无法保存专业选题组");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    /**
     * 删除系部
     *
     * @param request 请求结构
     */
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/delete/dept")
    public BaseResponse<Boolean> deleteDept(@RequestBody DeleteDeptRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String deptName = request.getDeptName();
        ThrowUtils.throwIf(deptName == null, CodeBindMessageEnums.PARAMS_ERROR, "系部名称不能为空");

        // 保证先删除专业才能删除系部
        List<Project> projectList = projectService.list(new QueryWrapper<Project>().eq("deptName", deptName));
        if (!projectList.isEmpty()) {
            String projectNames = projectList.stream().map(Project::getProjectName).collect(Collectors.joining(", "));
            ThrowUtils.throwIf(true, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "先删除属于该系部的所有专业（" + projectNames + "）后才能删除该系部");
        }

        // 保证先删除相关角色才能删除系部
        List<User> userList = userService.list(new QueryWrapper<User>().eq("dept", deptName));
        if (!userList.isEmpty()) {
            String userNames = userList.stream().limit(5).map(User::getUserName).collect(Collectors.joining(", "));
            if (userList.size() > 5) {
                userNames += "...";
            }
            ThrowUtils.throwIf(true, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "先删除属于该系部的所有角色（" + userNames + "）后才能删除该系部");
        }

        // 保证先删除相关选题才能删除系部
        List<Topic> topicList = topicService.list(new QueryWrapper<Topic>().eq("deptName", deptName));
        if (!topicList.isEmpty()) {
            String topicNames = topicList.stream().limit(5).map(Topic::getTopic).collect(Collectors.joining(", "));
            if (topicList.size() > 5) {
                topicNames += "...";
            }
            ThrowUtils.throwIf(true, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "先删除属于该系部的所有选题（" + topicNames + "）后才能删除该系部");
        }

        // 删除系部
        return transactionTemplate.execute(transactionStatus -> {
            boolean resalt = deptService.remove(new QueryWrapper<Dept>().eq("deptName", deptName));
            ThrowUtils.throwIf(!resalt, CodeBindMessageEnums.NOT_FOUND_ERROR, "找不到该系部");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        });
    }

    /**
     * 删除专业
     *
     * @param request 请求结构
     */
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/delete/project")
    public BaseResponse<Boolean> deleteProject(@RequestBody DeleteProjectRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String projectName = request.getProjectName();
        ThrowUtils.throwIf(projectName == null, CodeBindMessageEnums.PARAMS_ERROR, "专业名称不能为空");

        // 保证先删除相关角色才能删除系部
        List<User> userList = userService.list(new QueryWrapper<User>().eq("project", projectName));
        if (!userList.isEmpty()) {
            String userNames = userList.stream().limit(5).map(User::getUserName).collect(Collectors.joining(", "));
            if (userList.size() > 5) {
                userNames += "...";
            }
            ThrowUtils.throwIf(true, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "先删除属于该专业的所有角色（" + userNames + "）后才能删除该专业");
        }

        // 删除专业
        return transactionTemplate.execute(transactionStatus -> {
            boolean resalt = projectService.remove(new QueryWrapper<Project>().eq("projectName", projectName));
            ThrowUtils.throwIf(!resalt, CodeBindMessageEnums.NOT_FOUND_ERROR, "找不到该专业");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        });
    }

    /**
     * 获取系部分页数据
     */
    @SaCheckLogin
    @PostMapping("/get/dept/page")
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    public BaseResponse<Page<Dept>> getDept(@RequestBody DeptQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1 || size > 100, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须在 1 到 100 之间");

        // 获取系部数据
        Page<Dept> deptPage = deptService.page(new Page<>(current, size), deptService.getQueryWrapper(request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, deptPage);
    }

    /**
     * 获取系部列表数据（非管理员只能获取和当前登陆用户系部相同的系部）
     */
    @SaCheckLogin
    @PostMapping("/get/dept/list")
    public BaseResponse<List<DeptVO>> getDeptList(@RequestBody DeptQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        // 获取当前用户
        User user = userService.userGetCurrentLoginUser();

        // 获取当前用户的系部
        String deptName = user.getDept();

        // 查询所有 dept 列表
        List<Dept> deptList = deptService.list(userService.userIsAdmin(user) ? null : new QueryWrapper<Dept>().eq("deptName", deptName));

        // 脱敏数据
        List<DeptVO> deptVOList = new ArrayList<>();
        for (Dept dept : deptList) {
            DeptVO deptVO = new DeptVO();
            deptVO.setLabel(dept.getDeptName());
            deptVO.setValue(dept.getDeptName());
            deptVOList.add(deptVO);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, deptVOList);
    }

    /**
     * 获取专业分页数据
     */
    @SaCheckLogin
    @PostMapping("/get/project/page")
    public BaseResponse<Page<Project>> getProject(@RequestBody ProjectQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1 || size > 100, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须在 1 到 100 之间");

        // 获取专业数据
        Page<Project> projectPage = projectService.page(new Page<>(current, size), projectService.getQueryWrapper(request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, projectPage);
    }

    /**
     * 获取专业列表数据（非管理员只能获取和当前登陆用户系部相同的系部）
     */
    @SaCheckLogin
    @PostMapping("/get/project/list")
    public BaseResponse<List<ProjectVO>> getProjectList(@RequestBody ProjectQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1 || size > 100, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须在 1 到 100 之间");

        // 获取专业数据
        Page<Project> projectPage = projectService.page(new Page<>(current, size), projectService.getQueryWrapper(request));
        List<ProjectVO> projectVOList = new ArrayList<>();
        for (Project project : projectPage.getRecords()) {
            final String projectName = project.getProjectName();
            final ProjectVO projectVO = new ProjectVO();
            projectVO.setLabel(projectName);
            projectVO.setValue(projectName);
            projectVOList.add(projectVO);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, projectVOList);
    }

    /// 选题题目相关接口 ///

    /**
     * 添加选题
     */
    @SaCheckLogin
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/add/topic")
    public BaseResponse<Long> addTopic(@RequestBody AddTopicRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String requestedTopicTitle = request.getTopic();
        ThrowUtils.throwIf(StringUtils.isBlank(requestedTopicTitle), CodeBindMessageEnums.PARAMS_ERROR, "题目标题不能为空");
        final String topicTitle = requestedTopicTitle.trim();
        ThrowUtils.throwIf(topicTitle.length() > 255, CodeBindMessageEnums.PARAMS_ERROR, "题目标题不能超过 255 个字符");

        String requestedTopicType = request.getType();
        ThrowUtils.throwIf(StringUtils.isBlank(requestedTopicType), CodeBindMessageEnums.PARAMS_ERROR, "题目类型不能为空");
        final String topicType = requestedTopicType.trim();
        ThrowUtils.throwIf(topicType.length() > 255, CodeBindMessageEnums.PARAMS_ERROR, "题目类型不能超过 255 个字符");

        final String topicContent = request.getDescription();
        ThrowUtils.throwIf(StringUtils.isBlank(topicContent) || topicContent.trim().length() < 5, CodeBindMessageEnums.PARAMS_ERROR, "题目描述不能为空, 并且不能少于 5 个字符");

        final String topicRequirement = request.getRequirement();
        ThrowUtils.throwIf(StringUtils.isBlank(topicRequirement), CodeBindMessageEnums.PARAMS_ERROR, "题目要求不能为空");

        final int topicCapacity = request.getAmount() == null ? 1 : request.getAmount();
        ThrowUtils.throwIf(topicCapacity < 1 || topicCapacity > 100, CodeBindMessageEnums.PARAMS_ERROR, "题目人数必须在 1 到 100 之间");

        Topic oldTopic = topicService.getOne(new QueryWrapper<Topic>().eq("topic", topicTitle));
        ThrowUtils.throwIf(oldTopic != null, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "该选题已存在, 请不要重复添加");

        // 获取当前用户
        User user = userService.userGetCurrentLoginUser();

        return transactionTemplate.execute(transactionStatus -> {
            User loginUser = userMapper.selectByIdForUpdate(user.getId());
            ThrowUtils.throwIf(loginUser == null || !userService.userIsTeacher(loginUser), CodeBindMessageEnums.NO_AUTH_ERROR, "当前教师账号不存在");
            String teacherDept = loginUser.getDept();
            ThrowUtils.throwIf(StringUtils.isBlank(teacherDept), CodeBindMessageEnums.PARAMS_ERROR, "当前教师账号未配置所属系部");
            ThrowUtils.throwIf(
                    StringUtils.isNotBlank(request.getDeptName()) && !teacherDept.equals(request.getDeptName().trim()),
                    CodeBindMessageEnums.NO_AUTH_ERROR,
                    "不能为其他系部发布题目"
            );

            teacherGroupService.validate(loginUser.getUserAccount(), StringUtils.trimToNull(request.getTopicGroup()), null);

            Integer topicAmount = loginUser.getTopicAmount();
            ThrowUtils.throwIf(topicAmount == null || topicAmount <= 0, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "剩余出题数量不足, 请不要继续添加题目");

            Topic topic = new Topic();
            BeanUtils.copyProperties(request, topic);
            topic.setTopic(topicTitle);
            topic.setType(topicType);
            topic.setDescription(topicContent.trim());
            topic.setRequirement(topicRequirement.trim());
            topic.setTeacherName(loginUser.getUserName());
            topic.setTeacherAccount(loginUser.getUserAccount());
            topic.setDeptName(teacherDept);
            topic.setDeptTeacher("");
            topic.setTopicGroup(StringUtils.trimToNull(request.getTopicGroup()));
            topic.setSurplusQuantity(topicCapacity);
            boolean result = topicService.save(topic);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "无法添加新的选题");

            loginUser.setTopicAmount(topicAmount - 1);
            boolean teacherUpdated = userService.updateById(loginUser);
            ThrowUtils.throwIf(!teacherUpdated, CodeBindMessageEnums.OPERATION_ERROR, "无法更新教师出题额度");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, topic.getId());
        });
    }

    /**
     * 删除选题
     */
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/delete/topic")
    public BaseResponse<Boolean> deleteTopic(@RequestBody DeleteTopicRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long id = request.getId();
        ThrowUtils.throwIf(id == null, CodeBindMessageEnums.PARAMS_ERROR, "id 不能为空");
        assert id != null;
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "id 必须是正整数");

        User loginUser = userService.userGetCurrentLoginUser();
        return transactionTemplate.execute(transactionStatus -> {
            User lockedTeacher = userMapper.selectByIdForUpdate(loginUser.getId());
            ThrowUtils.throwIf(lockedTeacher == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "当前教师不存在");

            Topic topic = topicMapper.selectByIdForUpdate(id);
            ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "对应的选题不存在");
            ThrowUtils.throwIf(!isTopicOwner(lockedTeacher, topic), CodeBindMessageEnums.NO_AUTH_ERROR, "只能删除自己发布的题目");

            boolean topicRemoveResult = topicService.removeById(id);
            ThrowUtils.throwIf(!topicRemoveResult, CodeBindMessageEnums.OPERATION_ERROR, "无法删除题目");
            studentTopicSelectionService.remove(new QueryWrapper<StudentTopicSelection>().eq("topicId", id));

            lockedTeacher.setTopicAmount(lockedTeacher.getTopicAmount() + 1);
            boolean teacherUpdated = userService.updateById(lockedTeacher);
            ThrowUtils.throwIf(!teacherUpdated, CodeBindMessageEnums.OPERATION_ERROR, "无法恢复教师出题额度");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        });
    }

    /**
     * 获取教师题目上限
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/get/teacher/topicAmount")
    public BaseResponse<Integer> getTeacherTopicAmount(@RequestBody GetTeacherTopicAmountRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long teacherId = request.getTeacherId();
        ThrowUtils.throwIf(teacherId == null || teacherId <= 0, CodeBindMessageEnums.PARAMS_ERROR, "教师标识不合法");

        // 获取教师信息
        User teacher = userService.getById(teacherId);
        ThrowUtils.throwIf(teacher == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "教师不存在");
        assert teacher != null;
        ThrowUtils.throwIf(!teacher.getUserRole().equals(UserRoleEnum.TEACHER.getCode()), CodeBindMessageEnums.PARAMS_ERROR, "该用户不是教师");

        // 返回教师题目上限
        return TheResult.success(CodeBindMessageEnums.SUCCESS, teacher.getTopicAmount());
    }

    /**
     * 修改教师题目上限
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/set/teacher/topicAmount")
    public BaseResponse<Boolean> setTeacherTopicAmount(@RequestBody SetTeacherTopicAmountRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long teacherId = request.getTeacherId();
        ThrowUtils.throwIf(teacherId == null || teacherId <= 0, CodeBindMessageEnums.PARAMS_ERROR, "教师标识不合法");
        assert teacherId != null;

        Integer topicAmount = request.getTopicAmount();
        ThrowUtils.throwIf(topicAmount == null || topicAmount < 0 || topicAmount > 20, CodeBindMessageEnums.PARAMS_ERROR, "题目上限数量必须在 0-20 之间");
        assert topicAmount != null;

        // 获取教师信息
        User teacher = userService.getById(teacherId);
        ThrowUtils.throwIf(teacher == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "教师不存在");
        assert teacher != null;
        ThrowUtils.throwIf(!teacher.getUserRole().equals(UserRoleEnum.TEACHER.getCode()), CodeBindMessageEnums.PARAMS_ERROR, "该用户不是教师");

        // 检查教师目前的题目数量, 如果目前的题目数量已经到上限, 则不允许改小
        long currentTopicCount = topicService.count(new QueryWrapper<Topic>().eq("teacherAccount", teacher.getUserAccount()));
        ThrowUtils.throwIf(topicAmount < currentTopicCount, CodeBindMessageEnums.PARAMS_ERROR, "不能将题目上限设置为小于当前已出题目数量(" + currentTopicCount + ")");

        // 更新教师题目上限
        synchronized (String.valueOf(teacherId).intern()) { // 用教师 id 来加锁, 这样对同一个选题只能一个线程进行操作
            return transactionTemplate.execute(transactionStatus -> {
                teacher.setTopicAmount(topicAmount);
                boolean result = userService.updateById(teacher);
                ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "更新教师题目上限失败");
                return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
            });
        }
    }

    /**
     * 审核题目或重新审核题目
     */
    @SaCheckLogin
    @SaCheckRole(value = {"dept", "teacher"}, mode = SaMode.OR)
    @PostMapping("/check/topic")
    public BaseResponse<Boolean> checkTopic(@RequestBody CheckTopicRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long id = request.getId();
        ThrowUtils.throwIf(id == null, CodeBindMessageEnums.PARAMS_ERROR, "选题 id 不能为空");
        assert id != null;
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "选题 id 必须是正整数");

        Integer status = request.getStatus();
        ThrowUtils.throwIf(status == null, CodeBindMessageEnums.PARAMS_ERROR, "选题状态不能为空");
        assert status != null;
        TopicStatusEnum statusEnum = TopicStatusEnum.getEnums(status);
        ThrowUtils.throwIf(statusEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "未知的选题状态");

        String reason = request.getReason();
        ThrowUtils.throwIf(reason != null && reason.length() > TopicConstant.MAX_REASON_SIZE, CodeBindMessageEnums.PARAMS_ERROR, "理由过长, 不能超过 1024 符");

        User loginUser = userService.userGetCurrentLoginUser();
        return transactionTemplate.execute(transactionStatus -> {
            Topic topic = topicMapper.selectByIdForUpdate(id);
            ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "对应的选题不存在, 无需进行审核");
            ThrowUtils.throwIf(
                    !isAllowedTopicStatusTransition(loginUser, topic, statusEnum),
                    CodeBindMessageEnums.NO_AUTH_ERROR,
                    "无权执行该题目状态变更"
            );

            boolean rejected = statusEnum == TopicStatusEnum.REJECTED;
            ThrowUtils.throwIf(
                    rejected && StringUtils.isBlank(reason),
                    CodeBindMessageEnums.PARAMS_ERROR,
                    "打回题目时必须填写理由"
            );

            if (userService.userIsDept(loginUser)) {
                topic.setDeptTeacher(loginUser.getUserName());
            }
            topic.setStatus(statusEnum.getCode());
            topic.setReason(rejected ? reason : "");
            boolean result = topicService.updateById(topic);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.OPERATION_ERROR, "更新题目状态失败");

            if (rejected) {
                User teacher = userService.getOne(new QueryWrapper<User>()
                        .eq("userAccount", topic.getTeacherAccount())
                        .eq("userRole", UserRoleEnum.TEACHER.getCode())
                );
                if (teacher != null && StringUtils.isNotBlank(teacher.getEmail())) {
                    mailService.sendReasonMail(teacher.getEmail(), "毕业设计选题系统", topic.getReason());
                }
            }
            return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
        });
    }

    /**
     * 根据题目 id 列表添加开放的开始时间和结束时间来发布选题列表
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/set/time/by/id")
    public BaseResponse<String> setTimeById(@RequestBody SetTimeRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        List<Long> topicIds = validateTopicIds(request.getTopicIds());

        Date startTime = request.getStartTime();
        ThrowUtils.throwIf(startTime == null, CodeBindMessageEnums.PARAMS_ERROR, "请选择开始时间");
        assert startTime != null;

        Date endTime = request.getEndTime();
        ThrowUtils.throwIf(endTime == null, CodeBindMessageEnums.PARAMS_ERROR, "请选择结束时间");
        assert endTime != null;

        // 时间范围检查
        ThrowUtils.throwIf(startTime.after(endTime), CodeBindMessageEnums.PARAMS_ERROR, "开始时间不能晚于结束时间");

        // 并且时间范围均不能早于当前时间
        ThrowUtils.throwIf(startTime.before(new Date()), CodeBindMessageEnums.PARAMS_ERROR, "开始时间不能早于当前时间");
        ThrowUtils.throwIf(endTime.before(new Date()), CodeBindMessageEnums.PARAMS_ERROR, "结束时间不能早于当前时间");

        // 遍历选题列表开始设置开始时间和结束时间
        return transactionTemplate.execute(transactionStatus -> {
            for (Long topicId : topicIds) {
                Topic topic = topicMapper.selectByIdForUpdate(topicId);
                ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "选中的题目不存在");
                ThrowUtils.throwIf(
                        !Objects.equals(topic.getStatus(), TopicStatusEnum.NOT_PUBLISHED.getCode())
                                && !Objects.equals(topic.getStatus(), TopicStatusEnum.PUBLISHED.getCode()),
                        CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                        "只有审核通过的题目可以设置开放时间"
                );
                boolean result = topicService.update(new UpdateWrapper<Topic>()
                        .eq("id", topicId)
                        .set("status", TopicStatusEnum.PUBLISHED.getCode())
                        .set("startTime", startTime)
                        .set("endTime", endTime));
                ThrowUtils.throwIf(!result, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "无法开放该选题，请联系系统管理员");
            }
            return TheResult.success(CodeBindMessageEnums.SUCCESS, "成功开放题目!");
        });
    }

    /**
     * 根据题目 id 列表取消以及发布的选题列表, 并且置为空时间
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/unset/time/by/id")
    public BaseResponse<String> unsetTimeById(@RequestBody UnSetTimeRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        List<Long> topicIds = validateTopicIds(request.getTopicIds());

        // 遍历选题列表开始取消开放
        return transactionTemplate.execute(transactionStatus -> {
            String message = "";
            for (Long topicId : topicIds) {
                Topic topic = topicMapper.selectByIdForUpdate(topicId);
                ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "选中的题目不存在");
                long activeSelectionCount = studentTopicSelectionService.count(
                        new QueryWrapper<StudentTopicSelection>()
                                .eq("topicId", topicId)
                                .in("status", StudentTopicSelectionStatusEnum.EN_PRESELECT.getCode(), StudentTopicSelectionStatusEnum.EN_SELECT.getCode())
                );
                if (activeSelectionCount == 0) {
                    boolean result = topicService.update(new UpdateWrapper<Topic>()
                            .eq("id", topicId)
                            .set("status", TopicStatusEnum.NOT_PUBLISHED.getCode())
                            .set("startTime", null)
                            .set("endTime", null));
                    ThrowUtils.throwIf(!result, CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "无法取消开放该选题，请联系系统管理员");
                } else {
                    log.debug("教师 {} 出的题目 {} - {} 已经被学生选择, 不允许取消发布, 本次跳过取消发布", topic.getTeacherName(), topic.getId(), topic.getTopic());
                    message = " " + message + topic.getTeacherName() + topic.getId() + topic.getTopic();
                }
            }
            return TheResult.success(CodeBindMessageEnums.SUCCESS, "成功取消发布!" + (StringUtils.isNotBlank(message) ? message : ""));
        });
    }

    /**
     * 根据题目 id 进行预先选题的操作 (确认预先选题和取消预先选题)
     */
    @SuppressWarnings({"UnaryPlus", "DataFlowIssue"})
    @SaCheckLogin
    @SaCheckRole(value = {"student"}, mode = SaMode.OR)
    @PostMapping("/preselect/topic/by/id")
    public BaseResponse<Long> preSelectTopicById(@RequestBody SelectTopicByIdRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Long topicId = request.getId();
        ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.PARAMS_ERROR, "题目 id 不能为空");

        Integer status = request.getStatus();
        ThrowUtils.throwIf(status == null, CodeBindMessageEnums.PARAMS_ERROR, "操作状态不能为空");

        StudentTopicSelectionStatusEnum studentTopicSelectionStatusEnum = StudentTopicSelectionStatusEnum.getEnums(status);
        ThrowUtils.throwIf(studentTopicSelectionStatusEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "不存在这种状态");
        ThrowUtils.throwIf(
                studentTopicSelectionStatusEnum != StudentTopicSelectionStatusEnum.EN_PRESELECT
                        && studentTopicSelectionStatusEnum != StudentTopicSelectionStatusEnum.UN_PRESELECT,
                CodeBindMessageEnums.PARAMS_ERROR,
                "该接口只允许预选或取消预选"
        );

        User loginUser = userService.userGetCurrentLoginUser();
        return transactionTemplate.execute(transactionStatus -> {
            User lockedStudent = userMapper.selectByIdForUpdate(loginUser.getId());
            ThrowUtils.throwIf(
                    lockedStudent == null || !userService.userIsStudent(lockedStudent),
                    CodeBindMessageEnums.NO_AUTH_ERROR,
                    "当前账号不是有效学生账号"
            );

            Topic topic = topicMapper.selectByIdForUpdate(topicId);
            ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "该题目不存在");

            List<StudentTopicSelection> selections = studentTopicSelectionMapper.selectByUserForUpdate(lockedStudent.getUserAccount());
            StudentTopicSelection targetSelection = selections.stream()
                    .filter(item -> Objects.equals(item.getTopicId(), topicId))
                    .findFirst()
                    .orElse(null);

            if (studentTopicSelectionStatusEnum == StudentTopicSelectionStatusEnum.EN_PRESELECT) {
                validateStudentTopicGroup(lockedStudent, topic);
                boolean crossSelectionEnabled = switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH);
                ThrowUtils.throwIf(
                        !crossSelectionEnabled && !Objects.equals(lockedStudent.getDept(), topic.getDeptName()),
                        CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                        "不允许跨系部选题, 请等待开放"
                );
                ThrowUtils.throwIf(
                        crossSelectionEnabled && !this.isStudentAllowedCrossSelect(lockedStudent.getDept(), topic.getDeptName()),
                        CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                        "当前系统配置不允许预选该系部题目"
                );
                ThrowUtils.throwIf(targetSelection != null, CodeBindMessageEnums.OPERATION_ERROR, "不能重复预选该题目");
                ThrowUtils.throwIf(
                        selections.stream().anyMatch(this::isFinalSelection),
                        CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                        "您已经提交了题目, 不能再预选新的题目了"
                );
                ThrowUtils.throwIf(selections.size() >= 10, CodeBindMessageEnums.OPERATION_ERROR, "最多只能预选 10 个题目");
                ThrowUtils.throwIf(topic.getSurplusQuantity() <= 0, CodeBindMessageEnums.OPERATION_ERROR, "选题余量不足, 无法选择该题目");

                targetSelection = new StudentTopicSelection();
                targetSelection.setUserAccount(lockedStudent.getUserAccount());
                targetSelection.setTopicId(topicId);
                targetSelection.setStatus(StudentTopicSelectionStatusEnum.EN_PRESELECT.getCode());
                boolean saved = studentTopicSelectionService.save(targetSelection);
                ThrowUtils.throwIf(!saved, CodeBindMessageEnums.OPERATION_ERROR, "无法保存预选，请联系系统管理员");
                topic.setSelectAmount(topic.getSelectAmount() + 1);
            } else {
                ThrowUtils.throwIf(
                        targetSelection == null || !Objects.equals(targetSelection.getStatus(), StudentTopicSelectionStatusEnum.EN_PRESELECT.getCode()),
                        CodeBindMessageEnums.NOT_FOUND_ERROR,
                        "不存在需要取消预选的题目"
                );
                boolean removed = studentTopicSelectionService.removeById(targetSelection.getId());
                ThrowUtils.throwIf(!removed, CodeBindMessageEnums.OPERATION_ERROR, "无法取消预选");
                topic.setSelectAmount(Math.max(0, topic.getSelectAmount() - 1));
            }

            boolean topicUpdated = topicService.updateById(topic);
            ThrowUtils.throwIf(!topicUpdated, CodeBindMessageEnums.OPERATION_ERROR, "无法保存预选人数");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, topic.getId());
        });
    }

    /**
     * 根据题目 id 进行提交选题的操作 (确认提交选题和取消提交选题)
     */
    @SuppressWarnings({"UnaryPlus", "DataFlowIssue"})
    @SaCheckLogin
    @SaCheckRole(value = {"student"}, mode = SaMode.OR)
    @PostMapping("/select/topic/by/id")
    public BaseResponse<Long> selectTopicById(@RequestBody SelectTopicByIdRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");

        Long topicId = request.getId();
        ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.PARAMS_ERROR, "题目 id 不能为空");

        Integer status = request.getStatus();
        ThrowUtils.throwIf(status == null, CodeBindMessageEnums.PARAMS_ERROR, "请添加选择操作状态");

        StudentTopicSelectionStatusEnum statusEnums = StudentTopicSelectionStatusEnum.getEnums(status);
        ThrowUtils.throwIf(
                statusEnums != StudentTopicSelectionStatusEnum.EN_SELECT,
                CodeBindMessageEnums.PARAMS_ERROR,
                "该接口只允许确认选题，退选请使用退选接口"
        );

        User loginUser = userService.userGetCurrentLoginUser();
        return transactionTemplate.execute(transactionStatus -> confirmStudentSelection(loginUser, topicId));
    }

    BaseResponse<Long> confirmStudentSelection(User loginUser, Long topicId) {
        User lockedStudent = userMapper.selectByIdForUpdate(loginUser.getId());
        ThrowUtils.throwIf(
                lockedStudent == null || !userService.userIsStudent(lockedStudent),
                CodeBindMessageEnums.NO_AUTH_ERROR,
                "当前账号不是有效学生账号"
        );

        Topic topic = topicMapper.selectByIdForUpdate(topicId);
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "该题目不存在");
        validateStudentTopicGroup(lockedStudent, topic);
        ThrowUtils.throwIf(
                !Objects.equals(topic.getStatus(), TopicStatusEnum.PUBLISHED.getCode()),
                CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                "该题目未发布, 还不可以选中"
        );

        Date startTime = topic.getStartTime();
        Date endTime = topic.getEndTime();
        ThrowUtils.throwIf(startTime == null || endTime == null, CodeBindMessageEnums.PARAMS_ERROR, "题目开放时间未设置完整");
        Date now = new Date();
        ThrowUtils.throwIf(
                now.before(startTime) || now.after(endTime),
                CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                "当前不在选题开放范围内, 请等待管理员开放选题"
        );

        ThrowUtils.throwIf(
                !switchService.isEnabled(TopicConstant.SWITCH_SINGLE_CHOICE),
                CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                "当前模式为教师选择学生模式, 无法选题"
        );
        boolean crossSelectionEnabled = switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH);
        ThrowUtils.throwIf(
                !crossSelectionEnabled && !Objects.equals(lockedStudent.getDept(), topic.getDeptName()),
                CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                "不允许跨系部选题, 请等待开放"
        );
        ThrowUtils.throwIf(
                crossSelectionEnabled && !this.isStudentAllowedCrossSelect(lockedStudent.getDept(), topic.getDeptName()),
                CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                "当前系统配置不允许确认该系部题目"
        );

        List<StudentTopicSelection> selections = studentTopicSelectionMapper.selectByUserForUpdate(lockedStudent.getUserAccount());
        ThrowUtils.throwIf(
                selections.stream().anyMatch(this::isFinalSelection),
                CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                "您已经提交了题目, 不能再选新的题目了"
        );
        StudentTopicSelection selection = selections.stream()
                .filter(item -> Objects.equals(item.getTopicId(), topicId))
                .findFirst()
                .orElse(null);
        ThrowUtils.throwIf(
                selection == null || !Objects.equals(selection.getStatus(), StudentTopicSelectionStatusEnum.EN_PRESELECT.getCode()),
                CodeBindMessageEnums.NOT_FOUND_ERROR,
                "您还没有预选该题目"
        );
        ThrowUtils.throwIf(topic.getSurplusQuantity() <= 0, CodeBindMessageEnums.OPERATION_ERROR, "余量不足无法选择该题目");

        selection.setStatus(StudentTopicSelectionStatusEnum.EN_SELECT.getCode());
        boolean selectionUpdated = studentTopicSelectionService.updateById(selection);
        ThrowUtils.throwIf(!selectionUpdated, CodeBindMessageEnums.OPERATION_ERROR, "无法提交选题");

        topic.setSurplusQuantity(topic.getSurplusQuantity() - 1);
        boolean topicUpdated = topicService.updateById(topic);
        ThrowUtils.throwIf(!topicUpdated, CodeBindMessageEnums.OPERATION_ERROR, "无法更新题目余量");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, selection.getId());
    }

    /**
     * 更新选题信息
     */
    @SaCheckLogin
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/update/topic")
    public BaseResponse<String> updateTopic(@RequestBody UpdateTopicRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String topicName = request.getTopicName();
        ThrowUtils.throwIf(StringUtils.isBlank(topicName), CodeBindMessageEnums.PARAMS_ERROR, "需要修改题目时必须传该题目的标题");

        String type = request.getType();
        ThrowUtils.throwIf(StringUtils.isBlank(type), CodeBindMessageEnums.PARAMS_ERROR, "题目类型不能为空");

        String description = request.getDescription();
        ThrowUtils.throwIf(StringUtils.isBlank(description), CodeBindMessageEnums.PARAMS_ERROR, "题目描述不能为空");

        String requirement = request.getRequirement();
        ThrowUtils.throwIf(StringUtils.isBlank(requirement), CodeBindMessageEnums.PARAMS_ERROR, "题目要求不能为空");

        // 获取当前登陆的教师
        User loginUser = userService.userGetCurrentLoginUser();

        Topic ownedTopic = topicService.getOne(
                new QueryWrapper<Topic>()
                        .eq("topic", topicName)
                        .eq("teacherAccount", loginUser.getUserAccount())
        );
        ThrowUtils.throwIf(ownedTopic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "未找到当前教师名下的题目");

        return transactionTemplate.execute(transactionStatus -> {
            userMapper.selectByIdForUpdate(loginUser.getId());
            Topic topic = topicMapper.selectByIdForUpdate(ownedTopic.getId());
            ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "题目不存在");
            ThrowUtils.throwIf(!isTopicOwner(loginUser, topic), CodeBindMessageEnums.NO_AUTH_ERROR, "只能修改自己的题目");
            ThrowUtils.throwIf(
                    Objects.equals(topic.getStatus(), TopicStatusEnum.PUBLISHED.getCode()),
                    CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                    "已发布的题目不允许修改"
            );

            teacherGroupService.validate(loginUser.getUserAccount(), StringUtils.trimToNull(request.getTopicGroup()), topic.getId());
            topic.setType(type);
            topic.setDescription(description);
            topic.setRequirement(requirement);
            topic.setTopicGroup(StringUtils.trimToNull(request.getTopicGroup()));
            topic.setStatus(TopicStatusEnum.PENDING_REVIEW.getCode());
            topic.setReason("");
            boolean result = topicService.updateById(topic);
            ThrowUtils.throwIf(!result, CodeBindMessageEnums.SYSTEM_ERROR, "更新失败");
            return TheResult.success(CodeBindMessageEnums.SUCCESS, "更新成功");
        });
    }

    /**
     * 教师直接帮助学生确认提交题目
     */
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/select/student")
    public BaseResponse<String> selectStudent(@RequestBody SelectStudentRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 检查当前单选模式能否可预选
        ThrowUtils.throwIf(switchService.isEnabled(TopicConstant.SWITCH_SINGLE_CHOICE), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "当前模式为学生选择教师模式, 无法双选");

        // 检查请求参数是否为空
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String userAccount = request.getUserAccount();
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), CodeBindMessageEnums.PARAMS_ERROR, "用户账号不能为空");

        String topicName = request.getTopic();
        ThrowUtils.throwIf(StringUtils.isBlank(topicName), CodeBindMessageEnums.PARAMS_ERROR, "课题名称不能为空");

        User loginTeacher = userService.userGetCurrentLoginUser();
        User student = userService.userIsExist(userAccount);
        ThrowUtils.throwIf(
                student == null || !userService.userIsStudent(student),
                CodeBindMessageEnums.NOT_FOUND_ERROR,
                "未找到对应的学生账号"
        );

        Topic topic = topicService.getOne(new QueryWrapper<Topic>()
                .eq("topic", topicName)
                .eq("teacherAccount", loginTeacher.getUserAccount())
        );
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "未找到当前教师名下的课题");
        return transactionTemplate.execute(
                transactionStatus -> assignStudentSelection(loginTeacher, student, topic.getId())
        );
    }

    BaseResponse<String> assignStudentSelection(User loginTeacher, User student, Long topicId) {
        User lockedStudent = userMapper.selectByIdForUpdate(student.getId());
        ThrowUtils.throwIf(
                lockedStudent == null || !userService.userIsStudent(lockedStudent),
                CodeBindMessageEnums.NOT_FOUND_ERROR,
                "未找到对应的学生账号"
        );

        Topic topic = topicMapper.selectByIdForUpdate(topicId);
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "未找到对应的课题");
        validateStudentTopicGroup(lockedStudent, topic);
        ThrowUtils.throwIf(!isTopicOwner(loginTeacher, topic), CodeBindMessageEnums.NO_AUTH_ERROR, "只能为自己的题目选择学生");
        ThrowUtils.throwIf(
                !Objects.equals(topic.getStatus(), TopicStatusEnum.PUBLISHED.getCode()),
                CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                "该课题尚未发布, 暂时无法选择学生"
        );

        List<StudentTopicSelection> selections = studentTopicSelectionMapper.selectByUserForUpdate(lockedStudent.getUserAccount());
        ThrowUtils.throwIf(
                selections.stream().anyMatch(this::isFinalSelection),
                CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                "该学生已经确定最终题目"
        );
        ThrowUtils.throwIf(topic.getSurplusQuantity() <= 0, CodeBindMessageEnums.OPERATION_ERROR, "题目余量不足");

        StudentTopicSelection selection = selections.stream()
                .filter(item -> Objects.equals(item.getTopicId(), topicId))
                .findFirst()
                .orElse(null);
        if (selection == null) {
            selection = new StudentTopicSelection();
            selection.setUserAccount(lockedStudent.getUserAccount());
            selection.setTopicId(topicId);
            selection.setStatus(StudentTopicSelectionStatusEnum.EN_SELECT.getCode());
            boolean saved = studentTopicSelectionService.save(selection);
            ThrowUtils.throwIf(!saved, CodeBindMessageEnums.OPERATION_ERROR, "保存学生选题信息失败");
            topic.setSelectAmount(topic.getSelectAmount() + 1);
        } else {
            ThrowUtils.throwIf(
                    !Objects.equals(selection.getStatus(), StudentTopicSelectionStatusEnum.EN_PRESELECT.getCode()),
                    CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                    "该学生的题目关联状态不允许确认"
            );
            selection.setStatus(StudentTopicSelectionStatusEnum.EN_SELECT.getCode());
            boolean updated = studentTopicSelectionService.updateById(selection);
            ThrowUtils.throwIf(!updated, CodeBindMessageEnums.OPERATION_ERROR, "更新学生选题信息失败");
        }

        topic.setSurplusQuantity(topic.getSurplusQuantity() - 1);
        boolean topicUpdated = topicService.updateById(topic);
        ThrowUtils.throwIf(!topicUpdated, CodeBindMessageEnums.OPERATION_ERROR, "更新课题剩余数量失败");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, String.valueOf(selection.getId()));
    }

    /**
     * 教师或学生直接取消提交题目
     */
    @SaCheckRole(value = {"teacher", "student"}, mode = SaMode.OR)
    @PostMapping("/withdraw")
    public BaseResponse<Boolean> withdraw(@RequestBody WithdrawRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long topicId = request.getId();
        ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.PARAMS_ERROR, "id 不能为空");
        ThrowUtils.throwIf(topicId <= 0, CodeBindMessageEnums.PARAMS_ERROR, "id 必须是正整数");

        User loginUser = userService.userGetCurrentLoginUser();
        boolean studentOperation = userService.userIsStudent(loginUser);
        String requestedAccount = request.getUserAccount();
        if (studentOperation) {
            ThrowUtils.throwIf(
                    StringUtils.isNotBlank(requestedAccount)
                            && !Objects.equals(requestedAccount, loginUser.getUserAccount()),
                    CodeBindMessageEnums.NO_AUTH_ERROR,
                    "学生只能退选自己的题目"
            );
            requestedAccount = loginUser.getUserAccount();
        } else {
            ThrowUtils.throwIf(!userService.userIsTeacher(loginUser), CodeBindMessageEnums.NO_AUTH_ERROR, "当前角色不能退选");
            ThrowUtils.throwIf(StringUtils.isBlank(requestedAccount), CodeBindMessageEnums.PARAMS_ERROR, "教师退选时必须指定学生账号");
        }

        User selectedStudent = userService.userIsExist(requestedAccount);
        ThrowUtils.throwIf(
                selectedStudent == null || !userService.userIsStudent(selectedStudent),
                CodeBindMessageEnums.NOT_FOUND_ERROR,
                "未找到对应的学生账号"
        );
        return transactionTemplate.execute(
                transactionStatus -> withdrawSelection(loginUser, selectedStudent, topicId, studentOperation)
        );
    }

    BaseResponse<Boolean> withdrawSelection(User actor, User student, Long topicId, boolean studentOperation) {
        User lockedStudent = userMapper.selectByIdForUpdate(student.getId());
        ThrowUtils.throwIf(lockedStudent == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "学生账号不存在");

        Topic topic = topicMapper.selectByIdForUpdate(topicId);
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "未找到对应的课题, 无需退选");
        if (studentOperation) {
            ThrowUtils.throwIf(
                    !Objects.equals(actor.getId(), lockedStudent.getId()),
                    CodeBindMessageEnums.NO_AUTH_ERROR,
                    "学生只能退选自己的题目"
            );
        } else {
            ThrowUtils.throwIf(!isTopicOwner(actor, topic), CodeBindMessageEnums.NO_AUTH_ERROR, "只能从自己的题目中退选学生");
        }

        StudentTopicSelection selection = studentTopicSelectionMapper.selectByUserAndTopicForUpdate(
                lockedStudent.getUserAccount(), topicId
        );
        ThrowUtils.throwIf(
                selection == null || !isFinalSelection(selection),
                CodeBindMessageEnums.NOT_FOUND_ERROR,
                "未找到该学生的最终选题记录"
        );

        if (switchService.isEnabled(TopicConstant.TOPIC_LOCK)) {
            String lockTimestampValue = redisManager.getValue(TopicConstant.TOPIC_LOCK_TIME);
            ThrowUtils.throwIf(StringUtils.isBlank(lockTimestampValue), CodeBindMessageEnums.SYSTEM_ERROR, "退选锁定时间未配置");
            long lockTimestamp = Long.parseLong(lockTimestampValue) * 1000;
            ThrowUtils.throwIf(selection.getUpdateTime() == null, CodeBindMessageEnums.SYSTEM_ERROR, "选题记录更新时间缺失");
            ThrowUtils.throwIf(
                    selection.getUpdateTime().getTime() < lockTimestamp,
                    CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                    "管理员已锁定该选题，如需退选请联系管理员"
            );
        }

        restoreTopicCounters(topic, selection);
        boolean topicUpdated = topicService.updateById(topic);
        ThrowUtils.throwIf(!topicUpdated, CodeBindMessageEnums.OPERATION_ERROR, "无法恢复题目余量");

        boolean selectionRemoved = studentTopicSelectionService.removeById(selection.getId());
        ThrowUtils.throwIf(!selectionRemoved, CodeBindMessageEnums.OPERATION_ERROR, "删除学生选题记录失败");

        if (!studentOperation && StringUtils.isNotBlank(lockedStudent.getEmail())) {
            mailService.sendTopicMail(
                    lockedStudent.getEmail(),
                    "毕业设计选题系统",
                    "您被退选题目 [" + topic.getTopic() + "]，操作人为 " + actor.getUserName()
            );
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    /**
     * 获取选择了自己题目的学生
     */
    @SaCheckLogin
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/get/select/topic/by/id")
    public BaseResponse<List<User>> getSelectTopicById(@RequestBody GetSelectTopicById request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long id = request.getId();
        ThrowUtils.throwIf(id == null, CodeBindMessageEnums.PARAMS_ERROR, "id 不能为空");
        ThrowUtils.throwIf(id <= 0, CodeBindMessageEnums.PARAMS_ERROR, "id 必须是正整数");

        User loginTeacher = userService.userGetCurrentLoginUser();
        Topic topic = topicService.getById(id);
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "题目不存在");
        ThrowUtils.throwIf(!isTopicOwner(loginTeacher, topic), CodeBindMessageEnums.NO_AUTH_ERROR, "只能查看自己题目的学生");

        // 找到对应的学生题目关联记录
        List<StudentTopicSelection> list = studentTopicSelectionService.list(new QueryWrapper<StudentTopicSelection>()
                .eq("topicId", id)
                .eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode())
        );

        // 获取用户数据
        List<User> userList = new ArrayList<>();
        for (StudentTopicSelection student : list) {
            final String userAccount = student.getUserAccount();
            final User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
            if (user == null) {
                continue;
            }
            userList.add(user);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userList);
    }

    /**
     * 获取选题分页数据
     */
    @SaCheckLogin
    // @CacheSearchOptimization(ttl = 15, modelClass = Topic.class)
    @PostMapping("/get/topic/page")
    public BaseResponse<Page<Topic>> getTopicList(@RequestBody TopicQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码号必须大于 0");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1 || size > 100, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须在 1 到 100 之间");

        User loginUser = userService.userGetCurrentLoginUser();
        UserRoleEnum loginRole = loginUser == null || loginUser.getUserRole() == null
                ? null
                : UserRoleEnum.getEnums(loginUser.getUserRole());
        ThrowUtils.throwIf(
                loginRole == null || UserRoleEnum.BAN_ROLE.equals(loginRole),
                CodeBindMessageEnums.NO_AUTH_ERROR,
                "当前账号无权查看题目"
        );

        // 获取查询条件
        QueryWrapper<Topic> queryWrapper = topicService.getQueryWrapper(request);

        // 如果是管理员, 则支持获取没有任何人选中的题目
        if (UserRoleEnum.ADMIN.equals(loginRole)) {
            Boolean isNoOneSelectedTopic = request.getIsNoOneSelectedTopic();
            queryWrapper.eq(isNoOneSelectedTopic != null, "surplusQuantity", Boolean.TRUE.equals(isNoOneSelectedTopic) ? 1 : 0);
        } else if (UserRoleEnum.DEPT.equals(loginRole)) {
            // 如果是主任只看到本系部的选题
            queryWrapper.eq("deptName", requireDepartment(loginUser));
            queryWrapper.eq("topicGroup", requireUserGroup(loginUser));
        } else if (UserRoleEnum.TEACHER.equals(loginRole)) {
            // 如果是老师, 只看到自己负责的选题
            queryWrapper.eq("teacherAccount", loginUser.getUserAccount());
        } else if (UserRoleEnum.STUDENT.equals(loginRole)) {
            ThrowUtils.throwIf(!switchService.isEnabled(TopicConstant.VIEW_TOPIC_SWITCH), CodeBindMessageEnums.NOT_FOUND_ERROR, "当前时间学生无法查看选题, 请等待系统开放");
            // 如果是学生, 看是否开启跨选, 如果此时不允许跨选则不允许看到和当前登陆用户不同系部的教师
            if (!switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH)) {
                queryWrapper.eq("deptName", loginUser.getDept());
            }

            // 而且只能看到审核通过和已经发布的题目
            queryWrapper.in("status", TopicStatusEnum.NOT_PUBLISHED.getCode(), TopicStatusEnum.PUBLISHED.getCode()); // 学生只能查看已经审核通过和已经发布的选题
        }

        // 获取选题数据
        Page<Topic> topicPage = topicService.page(new Page<>(current, size), queryWrapper);

        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicPage);
    }

    /**
     * 获取当前的选题情况 (只能获取和当前登陆用户系部相同的选题)
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin", "dept"}, mode = SaMode.OR)
    @PostMapping("/get/select/topic/situation")
    public BaseResponse<SituationVO> getSelectTopicSituation() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 获取当前登陆用户
        User loginUser = userService.userGetCurrentLoginUser();
        if (userService.userIsDept(loginUser)) {
            requireDepartment(loginUser);
        }

        // 获取总人数
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .eq("userRole", UserRoleEnum.STUDENT.getCode()) // 只获取学生记录
                .eq(!userService.userIsAdmin(loginUser), "dept", loginUser.getDept()) // 获取当前登陆用户系部相同的学生, 但是管理员可以获取所有学生
                ;
        int totalStudents = (int) userService.count(queryWrapper);
        List<User> userList = userService.list(queryWrapper);

        // 获取已选题人数
        int selectedStudents = 0;
        for (User user : userList) {
            final String userAccount = user.getUserAccount();
            selectedStudents += (int) studentTopicSelectionService
                    .count(new QueryWrapper<StudentTopicSelection>()
                            .eq("userAccount", userAccount) // 获取当前用户的记录
                            .eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode()) // 查询状态为已选题的
                    );
        }

        // 获取未选题人数
        int unselectedStudents = totalStudents - selectedStudents;

        // 封装返回数据
        SituationVO situationVO = new SituationVO();
        situationVO.setAmount(totalStudents);
        situationVO.setSelectAmount(selectedStudents);
        situationVO.setUnselectAmount(unselectedStudents);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, situationVO);
    }

    /**
     * 获取系部教师数据
     */
    @SaCheckLogin
    // @CacheSearchOptimization(ttl = 30, modelClass = DeptTeacherVO.class)
    @PostMapping("/get/dept/teacher")
    public BaseResponse<Page<DeptTeacherVO>> getTeacher(@RequestBody DeptTeacherQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "当前页码必须大于 0");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1 || size > 100, CodeBindMessageEnums.PARAMS_ERROR, "每页大小必须在 1 到 100 之间");

        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        String teacherName = request.getTeacherName();
        String deptName = request.getDeptName();

        // 获取当前登陆用
        User loginUser = userService.userGetCurrentLoginUser();

        // 如果是学生, 则必须检查此时是否允许允许学生查看题目
        if (userService.userIsStudent(loginUser)) {
            ThrowUtils.throwIf(!switchService.isEnabled(TopicConstant.VIEW_TOPIC_SWITCH), CodeBindMessageEnums.NOT_FOUND_ERROR, "当前时间学生无法查看选题, 请等待系统开放");
        }

        // 查询教师列表
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userRole", UserRoleEnum.TEACHER.getCode()); // 是教师的

        // 如果有教师姓名搜索条件, 则添加搜索条件
        if (StringUtils.isNotBlank(teacherName)) {
            userQueryWrapper.like("userName", teacherName);
        }

        // 如果有教师系部搜索条件, 则添加搜索条件
        if (StringUtils.isNotBlank(deptName)) {
            userQueryWrapper.like("dept", deptName);
        }

        // 如果此时不允许跨选则不允许看到和当前登陆用户不同系部的教师
        if (!switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH)) {
            userQueryWrapper.eq("dept", loginUser.getDept());
        }

        // 添加排序条件
        if (SqlUtils.validSortField(sortField)) {
            userQueryWrapper.orderBy(true, sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        }

        List<User> users = userService.list(userQueryWrapper); // 得到所有的教师

        // 遍历教师列表来创建返回的 Page 对象, 填充每位教师的选题情况
        List<DeptTeacherVO> teacherVOList = new ArrayList<>();
        for (User user : users) {
            // 获得教师的名字
            String userName = user.getUserName();

            // 获得教师系部
            String dept = user.getDept();

            // 获得教师的对应选题
            QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
            topicQueryWrapper
                    .eq("teacherAccount", user.getUserAccount())
                    .in("status", TopicStatusEnum.NOT_PUBLISHED.getCode(), TopicStatusEnum.PUBLISHED.getCode()); // 学生只能查看已经审核通过和已经发布的选题
            int count = (int) topicService.count(topicQueryWrapper);
            List<Topic> topicList = topicService.list(topicQueryWrapper);

            // 计算剩余数量和选择数量
            Integer selectAmount = 0;
            Integer surplusQuantity = 0;
            for (Topic topic : topicList) {
                selectAmount += topic.getSelectAmount();
                surplusQuantity += topic.getSurplusQuantity();
            }

            // 构建 DeptTeacherVO 对象
            if (count != 0) {
                DeptTeacherVO teacherVO = new DeptTeacherVO();
                teacherVO.setTeacherName(userName);
                teacherVO.setDeptName(dept);
                teacherVO.setSurplusQuantity(surplusQuantity);
                teacherVO.setSelectAmount(selectAmount);
                teacherVO.setTopicAmount(count);
                teacherVOList.add(teacherVO);
            }
        }

        // 对教师列表进行分页处理
        int total = teacherVOList.size();
        int fromIndex = (int) ((current - 1) * size);
        int toIndex = (int) Math.min(fromIndex + size, total);

        // 确保索引不越界
        List<DeptTeacherVO> pagedTeacherVOList = new ArrayList<>();
        if (fromIndex < total) {
            pagedTeacherVOList = teacherVOList.subList(fromIndex, toIndex);
        }

        // 构建分页对象
        Page<DeptTeacherVO> teacherPage = new Page<>(current, size);
        teacherPage.setRecords(pagedTeacherVOList);
        teacherPage.setTotal((long) total); // 设置总记录数
        return TheResult.success(CodeBindMessageEnums.SUCCESS, teacherPage);
    }

    /**
     * 获取当前登陆账号学生的预先选题
     */
    @SaCheckLogin
    @SaCheckRole(value = {"student"}, mode = SaMode.OR)
    @PostMapping("/get/preselect/topic")
    public BaseResponse<List<Topic>> getPreTopic() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 获取当前登陆用户
        User loginUser = userService.userGetCurrentLoginUser();

        // 获取查询条件
        String userAccount = loginUser.getUserAccount();
        QueryWrapper<StudentTopicSelection> queryWrapper = new QueryWrapper<StudentTopicSelection>()
                .eq("userAccount", userAccount)
                .eq("status", StudentTopicSelectionStatusEnum.EN_PRESELECT.getCode());

        // 查询对应的预先选题记录
        ThrowUtils.throwIf(userAccount == null, CodeBindMessageEnums.OPERATION_ERROR, "参数有问题");
        List<StudentTopicSelection> studentTopicSelectionList = studentTopicSelectionService.list(queryWrapper);

        ThrowUtils.throwIf(studentTopicSelectionList == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "当前没有预选的题目");
        assert studentTopicSelectionList != null;

        // 填充完整的返回体, 把关联对应的选题都拿到
        List<Long> topicIds = studentTopicSelectionList
                .stream()
                .map(StudentTopicSelection::getTopicId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Topic> topicList = topicService.listByIds(topicIds);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicList);
    }

    /**
     * 获取当前登陆账号学生的最终选题
     */
    @SaCheckLogin
    @SaCheckRole(value = {"student"}, mode = SaMode.OR)
    @PostMapping("get/select/topic")
    public BaseResponse<List<Topic>> getSelectTopic() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 获取当前登陆用户
        User loginUser = userService.userGetCurrentLoginUser();
        String userAccount = loginUser.getUserAccount();

        // 查找确认最终选题的记录
        StudentTopicSelection studentTopicSelection = studentTopicSelectionService.getOne(new QueryWrapper<StudentTopicSelection>().eq("userAccount", userAccount).eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode()));
        log.info("用户: {} 确认了自己是否有最终选题", userAccount);

        ThrowUtils.throwIf(studentTopicSelection == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "当前用户没有确认最终的选题");
        assert studentTopicSelection != null;

        // 封装最终的返回值
        Long topicId = studentTopicSelection.getTopicId();
        Topic topic = topicService.getById(topicId);
        List<Topic> topicList = new ArrayList<>();
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.OPERATION_ERROR, "不存在对应的题目，请联系系统管理员");
        topicList.add(topic);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicList);
    }

    /**
     * 获取最终选题记录的选中时间
     */
    @SaCheckLogin
    @SaCheckRole(value = {"student"}, mode = SaMode.OR)
    @PostMapping("/get/select/topic/choice_time")
    public BaseResponse<String> getSelectTopicTime(@RequestBody GetSelectTopicRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 检查参数
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Long topicId = request.getTopicId();
        ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.PARAMS_ERROR, "id 不能为空");
        assert topicId != null;
        ThrowUtils.throwIf(topicId <= 0, CodeBindMessageEnums.PARAMS_ERROR, "id 必须是正整数");

        // 获取当前登陆用户
        User loginUser = userService.userGetCurrentLoginUser();

        // 获取题目关联记录
        StudentTopicSelection studentTopicSelection = studentTopicSelectionService
                .getOne(new QueryWrapper<StudentTopicSelection>()
                        .eq("topicId", topicId)
                        .eq("userAccount", loginUser.getUserAccount())
                        .eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode())
                );

        ThrowUtils.throwIf(studentTopicSelection == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "当前学生没有确认该题目");
        ThrowUtils.throwIf(studentTopicSelection.getUpdateTime() == null, CodeBindMessageEnums.OPERATION_ERROR, "选题记录缺少确认时间，请联系系统管理员");
        long topicTimestamp = studentTopicSelection.getUpdateTime().getTime() / 1000;
        log.debug("获取关联题目最终选择时间: {}", topicTimestamp);

        return TheResult.success(CodeBindMessageEnums.SUCCESS, String.valueOf(topicTimestamp));
    }

    /**
     * 获取和当前登陆用户同系的没有选题的学生
     */
    @SaCheckLogin
    @SaCheckRole(value = {"dept"}, mode = SaMode.OR)
    @PostMapping("/get/unselect/topic/student/list")
    public BaseResponse<List<User>> getUnSelectTopicStudentList() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 获取当前登陆用户
        User loginUser = userService.userGetCurrentLoginUser();
        final String dept = requireDepartment(loginUser);

        // 获取所有学生用户
        final List<User> userList = userService.list(new QueryWrapper<User>().eq("userRole", UserRoleEnum.STUDENT.getCode()).eq(StringUtils.isNotBlank(dept), "dept", dept));

        // 获取所有已经选题的学生
        final List<StudentTopicSelection> selectedList = studentTopicSelectionService.list(
                new QueryWrapper<StudentTopicSelection>()
                        .eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode())
        );

        // 将已经选题的学生账号存入一个 Set
        Set<String> selectedUserAccounts = selectedList.stream().map(StudentTopicSelection::getUserAccount).collect(Collectors.toSet());

        // 筛选出未选题的学生
        List<User> unselectedUsers = userList.stream().filter(user -> !selectedUserAccounts.contains(user.getUserAccount())).collect(Collectors.toList());

        return TheResult.success(CodeBindMessageEnums.SUCCESS, unselectedUsers);
    }

    /**
     * 管理员获取题目
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/get/topic/list/by/admin")
    public BaseResponse<Page<Topic>> getTopicListByAdmin(@RequestBody TopicQueryByAdminRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码必须大于等于 1");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1 || size > 100, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须在 1 到 100 之间");

        Page<Topic> topicPage = topicService.page(new Page<>(current, size), topicService.getTopicQueryByAdminWrapper(request));
        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicPage);

    }

    /**
     * 分页获取用户封装列表
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1 || size > 20, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须在 1 到 20 之间");

        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页号必须大于等于 1");

        // 查询分页结果
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(request));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userVOPage);
    }

    /**
     * 获取用户列表数据
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/get/user/list")
    public BaseResponse<List<UserNameVO>> getUserList(@RequestBody GetUserListRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        Integer userRole = request.getUserRole();
        ThrowUtils.throwIf(userRole == null || UserRoleEnum.getEnums(userRole) == null, CodeBindMessageEnums.PARAMS_ERROR, "用户角色不存在");
        List<UserNameVO> userNameVO = new ArrayList<>();
        List<User> userList = userService.list(new QueryWrapper<User>().eq("userRole", userRole));
        for (User user : userList) {
            UserNameVO item = new UserNameVO();
            item.setUserName(user.getUserName());
            userNameVO.add(item);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userNameVO);
    }

    /**
     * 根据题目 id 获取学生
     */
    @SaCheckLogin
    @SaCheckRole(value = {"teacher"}, mode = SaMode.OR)
    @PostMapping("/get/student/by/topicId")
    public BaseResponse<List<User>> getStudentByTopicId(@RequestBody GetStudentByTopicId request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;
        Long topicId = request.getId();
        ThrowUtils.throwIf(topicId == null || topicId <= 0, CodeBindMessageEnums.PARAMS_ERROR, "题目 id 必须是正整数");

        User loginTeacher = userService.userGetCurrentLoginUser();
        Topic topic = topicService.getById(topicId);
        ThrowUtils.throwIf(topic == null, CodeBindMessageEnums.NOT_FOUND_ERROR, "题目不存在");
        ThrowUtils.throwIf(!isTopicOwner(loginTeacher, topic), CodeBindMessageEnums.NO_AUTH_ERROR, "只能查看自己题目的学生");

        List<StudentTopicSelection> studentList = studentTopicSelectionService.list(new QueryWrapper<StudentTopicSelection>()
                .eq("topicId", topicId)
                .eq("status", StudentTopicSelectionStatusEnum.EN_SELECT.getCode())
        );
        ArrayList<User> userList = new ArrayList<>();
        for (StudentTopicSelection student : studentList) {
            String userAccount = student.getUserAccount();
            User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
            if (user == null) {
                continue;
            }
            userList.add(user);
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, userList);
    }

    /**
     * 获取系部教师数据 to 审核
     */
    @SaCheckLogin
    @SaCheckRole(value = {"dept"}, mode = SaMode.OR)
    @PostMapping("/get/dept/teacher/by/admin")
    public BaseResponse<Page<DeptTeacherVO>> getTeacherByAdmin(@RequestBody DeptTeacherQueryRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        long current = request.getCurrent();
        ThrowUtils.throwIf(current < 1, CodeBindMessageEnums.PARAMS_ERROR, "页码必须大于等于 1");

        long size = request.getPageSize();
        ThrowUtils.throwIf(size < 1 || size > 100, CodeBindMessageEnums.PARAMS_ERROR, "页大小必须在 1 到 100 之间");

        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        User loginUser = userService.userGetCurrentLoginUser();

        String dept = requireDepartment(loginUser);

        // 查询用户列表
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("dept", dept).eq("userRole", UserRoleEnum.TEACHER.getCode()).orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        List<User> users = this.userService.list(userQueryWrapper);

        // 创建返回的 Page 对象
        List<DeptTeacherVO> teacherVOList = new ArrayList<>();
        for (User user : users) {
            String userName = user.getUserName();

            // 查询该用户的课题列表
            QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
            topicQueryWrapper.eq("teacherAccount", user.getUserAccount());
            topicQueryWrapper.eq("status", TopicStatusEnum.PENDING_REVIEW.getCode());
            int count = (int) topicService.count(topicQueryWrapper);
            List<Topic> topicList = topicService.list(topicQueryWrapper);

            // 计算剩余数量和选择数量
            Integer surplusQuantity = 0;
            Integer selectAmount = 0;
            for (Topic topic : topicList) {
                surplusQuantity += topic.getSurplusQuantity();
                selectAmount += topic.getSelectAmount();
            }
            if (count != 0) {
                // 构建 DeptTeacherVO 对象
                DeptTeacherVO teacherVO = new DeptTeacherVO();
                teacherVO.setTeacherName(userName);
                teacherVO.setSurplusQuantity(surplusQuantity);
                teacherVO.setSelectAmount(selectAmount);
                teacherVO.setTopicAmount(count);
                teacherVOList.add(teacherVO);
            }
            // 添加到列表中
        }

        // 对教师列表进行分页处理
        int total = teacherVOList.size();
        int fromIndex = (int) ((current - 1) * size);
        int toIndex = (int) Math.min(fromIndex + size, total);

        // 确保索引不越界
        List<DeptTeacherVO> pagedTeacherVOList = new ArrayList<>();
        if (fromIndex < total) {
            pagedTeacherVOList = teacherVOList.subList(fromIndex, toIndex);
        }

        // 构建分页对象
        Page<DeptTeacherVO> teacherPage = new Page<>(current, size);
        teacherPage.setRecords(pagedTeacherVOList);
        teacherPage.setTotal((long) total); // 设置总记录数
        return TheResult.success(CodeBindMessageEnums.SUCCESS, teacherPage);
    }

    /**
     * 获取题目审核等级
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin", "teacher"}, mode = SaMode.OR)
    @PostMapping("/get/topic/review_level")
    public BaseResponse<AIResult> getTopicReviewLevel(@RequestBody GetTopicReviewLevelRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 参数检查
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        String topicTitle = StringUtils.trim(request.getTopic());
        ThrowUtils.throwIf(StringUtils.isBlank(topicTitle), CodeBindMessageEnums.PARAMS_ERROR, "题目标题不能为空");
        ThrowUtils.throwIf(topicTitle.length() > 255, CodeBindMessageEnums.PARAMS_ERROR, "题目标题不能超过 255 个字符");

        String topicContent = StringUtils.trim(request.getDescription());
        ThrowUtils.throwIf(StringUtils.isBlank(topicContent) || topicContent.length() < 5, CodeBindMessageEnums.PARAMS_ERROR, "题目描述不能为空, 并且不能少于 5 个字符");
        ThrowUtils.throwIf(topicContent.length() > 5000, CodeBindMessageEnums.PARAMS_ERROR, "题目描述不能超过 5000 个字符");

        // 获取当前登陆用户的 id 并且转化为 UUID
        User loginUser = userService.userGetCurrentLoginUser();
        Long id = loginUser.getId();
        boolean acquired = redisManager.tryAcquire(
                AI_REVIEW_RATE_LIMIT_PREFIX + id,
                30,
                24 * 60 * 60
        );
        ThrowUtils.throwIf(!acquired, CodeBindMessageEnums.FLOW_RULES, "AI 检测每天最多使用 30 次，请稍后再试");
        String factor = "work-topic-selection-backend"; // 调整因子，可以是随机值、时间戳、业务常量
        String raw = id + "-" + factor;
        String userId = UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8)).toString();

        String prompt = "题目标题：" + topicTitle + "\n题目描述：" + topicContent
                + "\n请判断题库中是否有相似题目，并给出约定格式的 JSON 响应。";
        AIResult aiResult = aiManager.sendAi(userId, prompt);
        ThrowUtils.throwIf(aiResult == null, CodeBindMessageEnums.OPERATION_ERROR, "AI 服务未返回有效的检测结果");
        return TheResult.success(CodeBindMessageEnums.SUCCESS, aiResult);
    }

    /**
     * 查询是否允许跨系状态
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/cross_topic")
    public BaseResponse<Boolean> getCrossTopicStatus() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        return TheResult.success(CodeBindMessageEnums.SUCCESS, switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH));
    }

    /**
     * 设置是否允许跨系开关
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/cross_topic")
    public BaseResponse<String> setCrossTopicStatus(@RequestParam boolean enabled) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        switchService.setEnabled(TopicConstant.CROSS_TOPIC_SWITCH, enabled);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, "跨系选题功能已" + (enabled ? "开启" : "关闭"));
    }

    /**
     * 查询学生查看选题状态
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/view_topic")
    public BaseResponse<Boolean> getViewTopicStatus() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        return TheResult.success(CodeBindMessageEnums.SUCCESS, switchService.isEnabled(TopicConstant.VIEW_TOPIC_SWITCH));
    }

    /**
     * 设置学生查看选题开关
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/view_topic")
    public BaseResponse<String> setViewTopicStatus(@RequestParam boolean enabled) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        switchService.setEnabled(TopicConstant.VIEW_TOPIC_SWITCH, enabled);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, (enabled ? "允许" : "禁止") + "学生查看选题");
    }

    /**
     * 查询单选模式切换状态
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/switch_single_choice")
    public BaseResponse<Boolean> getSwitchSingleChoiceStatus() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        return TheResult.success(CodeBindMessageEnums.SUCCESS, switchService.isEnabled(TopicConstant.SWITCH_SINGLE_CHOICE));
    }

    /**
     * 设置单选模式切换开关
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/switch_single_choice")
    public BaseResponse<String> setSwitchSingleChoiceStatus(@RequestParam boolean enabled) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        switchService.setEnabled(TopicConstant.SWITCH_SINGLE_CHOICE, enabled);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, "当前单选模式切换为" + (enabled ? "学生单选模式" : "教师单选模式"));
    }

    /**
     * 查询是否退选加锁状态
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin", "teacher", "student"}, mode = SaMode.OR)
    @GetMapping("/topic_lock")
    public BaseResponse<TopicLockVO> getTopicLock() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        TopicLockVO topicLockVO = new TopicLockVO();
        topicLockVO.setIslock(switchService.isEnabled(TopicConstant.TOPIC_LOCK));
        topicLockVO.setLockTime(redisManager.getValue(TopicConstant.TOPIC_LOCK_TIME));

        return TheResult.success(CodeBindMessageEnums.SUCCESS, topicLockVO);
    }

    /**
     * 设置是否退选加锁开关
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/topic_lock")
    public BaseResponse<String> setTopicLock(@RequestParam boolean enabled, String timestamp) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        if (enabled) {
            ThrowUtils.throwIf(StringUtils.isBlank(timestamp), CodeBindMessageEnums.PARAMS_ERROR, "请设置需要加锁的时间");
            long lockTimestamp;
            try {
                lockTimestamp = Long.parseLong(timestamp);
            } catch (NumberFormatException e) {
                throw new cn.com.edtechhub.worktopicselection.exception.BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "加锁时间格式不正确");
            }
            ThrowUtils.throwIf(lockTimestamp <= System.currentTimeMillis() / 1000, CodeBindMessageEnums.PARAMS_ERROR, "加锁时间必须晚于当前时间");
            redisManager.setValue(TopicConstant.TOPIC_LOCK_TIME, timestamp);
            switchService.setEnabled(TopicConstant.TOPIC_LOCK, true);
        } else {
            redisManager.deleteKey(TopicConstant.TOPIC_LOCK_TIME);
            switchService.setEnabled(TopicConstant.TOPIC_LOCK, false);
        }

        return TheResult.success(CodeBindMessageEnums.SUCCESS, "当前是否退选加锁为" + (enabled ? "禁止退选题目" : "允许退选题目"));
    }

    /**
     * 查看系部选跨选配置
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/get/dept/config")
    public BaseResponse<DeptConfigVO> getDeptConfig() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        DeptConfigVO deptConfigVO = new DeptConfigVO();
        if (switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH)) {
            Set<String> keys = redisManager.getKeysByPattern(TopicConstant.DEPT_CROSS_TOPIC_CONFIG + ":*");
            if (keys != null && !keys.isEmpty()) {
                Map<String, List<String>> enableSelectDeptsList = new HashMap<>();

                for (String key : keys) {
                    String value = redisManager.getValue(key);
                    if (value != null) {
                        // key 格式为 DEPT_CROSS_TOPIC_CONFIG:<deptId>
                        String objectDeptName = key.split(":")[1];

                        // value 存储的是 JSON 字符串，需要反序列化
                        List<String> enableSelectDepts = JSONUtil.toList(value, String.class);

                        enableSelectDeptsList.put(objectDeptName, enableSelectDepts);
                    }
                }

                deptConfigVO.setEnableSelectDeptsList(enableSelectDeptsList);
            }
        }

        return TheResult.success(CodeBindMessageEnums.SUCCESS, deptConfigVO);
    }

    /**
     * 设置系部选跨选配置
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/set/dept/config")
    public BaseResponse<Boolean> setDeptConfig(@RequestBody SetDeptConfigRequest request) throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 检查参数
        ThrowUtils.throwIf(request == null, CodeBindMessageEnums.PARAMS_ERROR, "请求体不能为空");
        assert request != null;

        // 取出没有开启跨系开关的情况
        ThrowUtils.throwIf(!switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "请先开启跨系开关后再配置选题规则");

        Map<String, List<String>> enableSelectDeptsList = request.getEnableSelectDeptsList();
        ThrowUtils.throwIf(enableSelectDeptsList == null || enableSelectDeptsList.isEmpty(), CodeBindMessageEnums.PARAMS_ERROR, "请至少选择一个系部后再配置");
        boolean hasRule = false;
        for (Map.Entry<String, List<String>> entry : enableSelectDeptsList.entrySet()) {
            ThrowUtils.throwIf(StringUtils.isBlank(entry.getKey()), CodeBindMessageEnums.PARAMS_ERROR, "配置中的系部名称不能为空");
            ThrowUtils.throwIf(entry.getValue() == null, CodeBindMessageEnums.PARAMS_ERROR, "系部可选范围不能为空");
            ThrowUtils.throwIf(deptService.getOne(new QueryWrapper<Dept>().eq("deptName", entry.getKey())) == null, CodeBindMessageEnums.PARAMS_ERROR, "配置中包含不存在的系部");
            for (String targetDept : entry.getValue()) {
                ThrowUtils.throwIf(StringUtils.isBlank(targetDept), CodeBindMessageEnums.PARAMS_ERROR, "可选系部名称不能为空");
                ThrowUtils.throwIf(deptService.getOne(new QueryWrapper<Dept>().eq("deptName", targetDept)) == null, CodeBindMessageEnums.PARAMS_ERROR, "配置中包含不存在的可选系部");
            }
            hasRule = hasRule || !entry.getValue().isEmpty();
        }
        ThrowUtils.throwIf(!hasRule, CodeBindMessageEnums.PARAMS_ERROR, "请至少选择一个系部后再配置");

        // 全部校验通过后再替换全量配置。
        Set<String> keys = redisManager.getKeysByPattern(TopicConstant.DEPT_CROSS_TOPIC_CONFIG + ":*");
        redisManager.deleteKeys(keys);

        // 配置规则
        for (Map.Entry<String, List<String>> entry : enableSelectDeptsList.entrySet()) {
            String objectDeptName = entry.getKey();
            List<String> enableSelectDepts = entry.getValue();
            if (!enableSelectDepts.isEmpty()) {
                redisManager.setValue(TopicConstant.DEPT_CROSS_TOPIC_CONFIG + ":" + objectDeptName, JSONUtil.toJsonStr(enableSelectDepts));
            }
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    /**
     * 清除系部选跨选配置
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @PostMapping("/del/dept/config")
    public BaseResponse<Boolean> delDeptConfig() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 必须开启跨选开关
        ThrowUtils.throwIf(!switchService.isEnabled(TopicConstant.CROSS_TOPIC_SWITCH), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "请先开启跨系开关后再清除跨选规则");

        // 清理所有的跨选规则
        Set<String> keys = redisManager.getKeysByPattern(TopicConstant.DEPT_CROSS_TOPIC_CONFIG + ":*");
        redisManager.deleteKeys(keys);
        return TheResult.success(CodeBindMessageEnums.SUCCESS, true);
    }

    /**
     * 查看系统的相关信息面板信息
     */
    @SaCheckLogin
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    @GetMapping("/get/system/info")
    public BaseResponse<TheSystemInfoVO> getSystemInfo() throws BlockException {
        // 流量控制
        String entryName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sentineManager.initFlowRules(entryName);
        try (com.alibaba.csp.sentinel.Entry ignored = SphU.entry(entryName)) {
        }

        // 创建存储系统信息的 VO 对象
        TheSystemInfoVO theSystemInfoVo = new TheSystemInfoVO();

        // 选题信息
        theSystemInfoVo.setTotalDeptCount(userService
                .lambdaQuery()
                .eq(User::getUserRole, 2)
                .count()
        );

        theSystemInfoVo.setTotalTeacherCount(userService
                .lambdaQuery()
                .eq(User::getUserRole, 1)
                .count()
        );

        theSystemInfoVo.setTotalStudentCount(userService
                .lambdaQuery()
                .eq(User::getUserRole, 0)
                .count()
        );

        theSystemInfoVo.setLoginUserCount(userService
                .lambdaQuery()
                .eq(User::getStatus, "老用户")
                .count()
        );

        theSystemInfoVo.setAuditPassTopicCount(topicService
                .lambdaQuery()
                .eq(Topic::getStatus, 0)
                .count()
        );

        theSystemInfoVo.setAuditBackTopicCount(topicService
                .lambdaQuery()
                .eq(Topic::getStatus, -2)
                .count()
        );

        theSystemInfoVo.setAuditTopicCount(topicService
                .lambdaQuery()
                .eq(Topic::getStatus, -1)
                .count()
        );

        theSystemInfoVo.setReleaseTopicCount(topicService
                .lambdaQuery()
                .eq(Topic::getStatus, 1)
                .count()
        );

        // 系统信息
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;

        double cpuLoad = sunOsBean.getSystemCpuLoad() * 100;
        theSystemInfoVo.setCpuUsage(new DecimalFormat("0.00").format(cpuLoad) + "%");

        long totalPhysical = sunOsBean.getTotalPhysicalMemorySize();
        long freePhysical = sunOsBean.getFreePhysicalMemorySize();
        long usedPhysical = totalPhysical - freePhysical;
        theSystemInfoVo.setMemoryUsage(formatSize(usedPhysical) + "/" + formatSize(totalPhysical));

        File root = new File("/");
        long totalDisk = root.getTotalSpace();
        long freeDisk = root.getFreeSpace();
        long usedDisk = totalDisk - freeDisk;
        theSystemInfoVo.setDiskUsage(formatSize(usedDisk) + "/" + formatSize(totalDisk));

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long usedHeap = heapUsage.getUsed();
        long maxHeap = heapUsage.getMax();
        theSystemInfoVo.setJvmMemoryUsage(formatSize(usedHeap) + "/" + formatSize(maxHeap));

        return TheResult.success(CodeBindMessageEnums.SUCCESS, theSystemInfoVo);
    }

    @SaCheckRole("teacher")
    @GetMapping("/teacher/groups")
    public BaseResponse<List<Map<String, Object>>> getTeacherGroups() {
        return TheResult.success(CodeBindMessageEnums.SUCCESS,
                teacherGroupService.groups(userService.userGetCurrentLoginUser().getUserAccount()));
    }

    /**
     * 批量查询指定教师的选题组额度, 供教师列表展示使用 (管理员可查全部, 系部主任仅限本系部)
     */
    @SaCheckRole(value = {"admin", "dept"}, mode = SaMode.OR)
    @PostMapping("/teacher/groups/batch")
    public BaseResponse<Map<String, List<Map<String, Object>>>> getTeacherGroupsBatch(@RequestBody TeacherGroupsBatchRequest request) {
        ThrowUtils.throwIf(request == null || request.getTeacherAccounts() == null,
                CodeBindMessageEnums.PARAMS_ERROR, "教师账号列表不能为空");
        List<String> accounts = request.getTeacherAccounts();
        User loginUser = userService.userGetCurrentLoginUser();
        if (Boolean.TRUE.equals(userService.userIsDept(loginUser))) {
            List<User> deptTeachers = userService.list(new QueryWrapper<User>()
                    .eq("userRole", UserRoleEnum.TEACHER.getCode())
                    .eq("dept", loginUser.getDept()));
            Set<String> allowedAccounts = deptTeachers.stream()
                    .map(User::getUserAccount)
                    .collect(Collectors.toSet());
            accounts = accounts.stream().filter(allowedAccounts::contains).collect(Collectors.toList());
        }
        return TheResult.success(CodeBindMessageEnums.SUCCESS, teacherGroupService.groupsBatch(accounts));
    }

    String requireUserGroup(User user) {
        Project project = projectService.getOne(new QueryWrapper<Project>().eq("projectName", user.getProject()));
        ThrowUtils.throwIf(project == null || StringUtils.isBlank(project.getGroupName()),
                CodeBindMessageEnums.NO_AUTH_ERROR, "当前专业未配置选题组");
        return project.getGroupName();
    }

    boolean isTopicOwner(User teacher, Topic topic) {
        return teacher != null
                && topic != null
                && Objects.equals(teacher.getUserRole(), UserRoleEnum.TEACHER.getCode())
                && StringUtils.isNotBlank(topic.getTeacherAccount())
                && Objects.equals(teacher.getUserAccount(), topic.getTeacherAccount());
    }

    boolean isAllowedTopicStatusTransition(User actor, Topic topic, TopicStatusEnum targetStatus) {
        if (actor == null || topic == null || targetStatus == null) {
            return false;
        }
        if (Objects.equals(actor.getUserRole(), UserRoleEnum.DEPT.getCode())) {
            return StringUtils.isNotBlank(actor.getDept())
                    && StringUtils.isNotBlank(topic.getDeptName())
                    && Objects.equals(actor.getDept(), topic.getDeptName())
                    && Objects.equals(requireUserGroup(actor), topic.getTopicGroup())
                    && Objects.equals(topic.getStatus(), TopicStatusEnum.PENDING_REVIEW.getCode())
                    && (targetStatus == TopicStatusEnum.NOT_PUBLISHED || targetStatus == TopicStatusEnum.REJECTED);
        }
        return isTopicOwner(actor, topic)
                && Objects.equals(topic.getStatus(), TopicStatusEnum.REJECTED.getCode())
                && targetStatus == TopicStatusEnum.PENDING_REVIEW;
    }

    boolean isFinalSelection(StudentTopicSelection selection) {
        return selection != null
                && Objects.equals(selection.getStatus(), StudentTopicSelectionStatusEnum.EN_SELECT.getCode());
    }

    boolean isActiveSelection(StudentTopicSelection selection) {
        return selection != null
                && (Objects.equals(selection.getStatus(), StudentTopicSelectionStatusEnum.EN_PRESELECT.getCode())
                || isFinalSelection(selection));
    }

    void restoreTopicCounters(Topic topic, StudentTopicSelection selection) {
        if (!isActiveSelection(selection)) {
            return;
        }
        if (isFinalSelection(selection)) {
            topic.setSurplusQuantity(topic.getSurplusQuantity() + 1);
        }
        topic.setSelectAmount(Math.max(0, topic.getSelectAmount() - 1));
    }

    static String requireDepartment(User user) {
        String dept = user == null ? null : StringUtils.trim(user.getDept());
        ThrowUtils.throwIf(StringUtils.isBlank(dept), CodeBindMessageEnums.NO_AUTH_ERROR, "当前账号未配置所属系部");
        return dept;
    }

    static List<Long> validateTopicIds(List<Long> topicIds) {
        ThrowUtils.throwIf(topicIds == null || topicIds.isEmpty(), CodeBindMessageEnums.PARAMS_ERROR, "请先选择题目");
        ThrowUtils.throwIf(topicIds.size() > 100, CodeBindMessageEnums.PARAMS_ERROR, "一次最多处理 100 个题目");
        for (Long topicId : topicIds) {
            ThrowUtils.throwIf(topicId == null || topicId <= 0, CodeBindMessageEnums.PARAMS_ERROR, "题目标识必须是正整数");
        }
        return topicIds.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 格式化数据单位
     */
    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", size / Math.pow(1024, exp), unit);
    }

    /**
     * 校验学生专业与题目适用选题组是否一致。
     *
     * <p>历史题目没有设置适用组时继续按原有规则处理；新题目设置了适用组后，
     * 学生必须存在专业且该专业已配置到同一个选题组。校验放在后端，避免绕过前端直接调用接口。</p>
     */
    void validateStudentTopicGroup(User student, Topic topic) {
        if (StringUtils.isBlank(topic.getTopicGroup())) {
            return;
        }
        ThrowUtils.throwIf(StringUtils.isBlank(student.getProject()), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "当前学生未配置专业，无法选择分组题目");
        Project project = projectService.getOne(new QueryWrapper<Project>().eq("projectName", student.getProject()));
        ThrowUtils.throwIf(project == null || StringUtils.isBlank(project.getGroupName()), CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR, "当前专业未配置选题组，无法选择该题目");
        ThrowUtils.throwIf(
                !Objects.equals(StringUtils.trimToNull(project.getGroupName()), StringUtils.trimToNull(topic.getTopicGroup())),
                CodeBindMessageEnums.ILLEGAL_OPERATION_ERROR,
                "当前专业不属于该题目适用的选题组"
        );
    }

    /**
     * 判断该系部的学生在跨选配置中是否允许跨选
     */
    private Boolean isStudentAllowedCrossSelect(String userDeptName, String objDeptName) {
        // 检查参数
        ThrowUtils.throwIf(StringUtils.isBlank(userDeptName), CodeBindMessageEnums.PARAMS_ERROR, "用户系部不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(objDeptName), CodeBindMessageEnums.PARAMS_ERROR, "目标系部不能为空");

        // 如果有存在配置就默认按照规则跨选, 不存在就直接允许跨选所有系部
        String value = redisManager.getValue(TopicConstant.DEPT_CROSS_TOPIC_CONFIG + ":" + userDeptName);
        if (value == null) {
            return true;
        }
        List<String> enableSelectDepts = JSONUtil.toList(value, String.class);
        return enableSelectDepts.contains(objDeptName);
    }

    /**
     * 美观 JSON 列表
     */
    private String beautifyList(String raw) {
        List<String> list = JSONUtil.toList(raw, String.class);
        return String.join("、", list);
    }

}
