package cn.com.edtechhub.worktopicselection.service;

import cn.com.edtechhub.worktopicselection.model.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.com.edtechhub.worktopicselection.model.dto.user.UserQueryRequest;
import cn.com.edtechhub.worktopicselection.model.vo.LoginUserVO;
import cn.com.edtechhub.worktopicselection.model.vo.UserVO;

import java.util.List;

/**
 * 用户服务
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
public interface UserService extends IService<User> {

    /**
     * 更具 userAccount 来检查一个用户在数据库中是否存在（已支持逻辑删除后的查询）
     *
     * @param userAccount 用户账号
     * @return User
     */
    User userIsExist(String userAccount);

    /**
     * 更具 userAccount 来检查一个用户在数据库中是否存在（已支持逻辑删除后的查询）
     *
     * @param userAccount 用户账号
     * @return User
     */
    User userIsExist(String userAccount, String userName);

    /**
     * 使用 BCrypt 散列新密码。
     */
    String encodePassword(String rawPassword);

    /**
     * 迁移旧散列时使用 BCrypt 编码；允许旧账号沿用不足 8 字节的历史密码完成登录升级。
     */
    String encodePasswordForMigration(String rawPassword);

    /**
     * 校验 BCrypt 密码，并在迁移期兼容旧版固定盐 MD5。
     */
    boolean matchesPassword(String rawPassword, String encodedPassword);

    /**
     * 判断数据库中的密码是否仍为旧版 MD5。
     */
    boolean needsPasswordUpgrade(String encodedPassword);

    /**
     * 生成只展示一次的随机临时密码。
     */
    String generateTemporaryPassword();

    /**
     * 校验 BCrypt 可安全处理的新密码长度。
     */
    boolean isPasswordValid(String rawPassword);

    // 获取当前登陆用户标识
    Long userGetCurrentLonginUserId();

    // 获取当前登陆用户会话
    User userGetCurrentLoginUser();

    // 检查用户是否是管理员
    Boolean userIsAdmin(User user);

    // 检查用户是否是主任
    Boolean userIsDept(User user);

    // 检查用户是否是教师
    Boolean userIsTeacher(User user);

    // 检查用户是否是学生
    Boolean userIsStudent(User user);

    // 获取查询条件
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    // TODO: 下面是旧代码可以被迁移到 UserVO 中

    /**
     * 获取脱敏的当前登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的指定用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息列表
     */
    List<UserVO> getUserVO(List<User> userList);

}
