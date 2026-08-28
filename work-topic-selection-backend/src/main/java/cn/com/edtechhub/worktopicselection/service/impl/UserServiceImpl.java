package cn.com.edtechhub.worktopicselection.service.impl;

import cn.com.edtechhub.worktopicselection.constant.CommonConstant;
import cn.com.edtechhub.worktopicselection.constant.UserConstant;
import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.mapper.UserMapper;
import cn.com.edtechhub.worktopicselection.model.dto.user.UserQueryRequest;
import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.model.enums.UserRoleEnum;
import cn.com.edtechhub.worktopicselection.model.vo.LoginUserVO;
import cn.com.edtechhub.worktopicselection.model.vo.UserVO;
import cn.com.edtechhub.worktopicselection.service.UserService;
import cn.com.edtechhub.worktopicselection.utils.SqlUtils;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Service
@Slf4j
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final String LOWERCASE = "abcdefghijkmnopqrstuvwxyz";

    private static final String DIGITS = "23456789";

    private static final String SPECIALS = "!@#$%*-_";

    private static final String TEMPORARY_PASSWORD_ALPHABET = UPPERCASE + LOWERCASE + DIGITS + SPECIALS;

    private static final int TEMPORARY_PASSWORD_LENGTH = 16;

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(CodeBindMessageEnums.PARAMS_ERROR, "请求参数为空");
        }
        String userAccount = userQueryRequest.getUserAccount();
        final Integer userRole = userQueryRequest.getUserRole();
        String userName = userQueryRequest.getUserName();
        String dept = userQueryRequest.getDept();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StringUtils.isNotBlank(dept), "dept", dept);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validUserSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);

        return queryWrapper;
    }

    @Override
    public Long userGetCurrentLonginUserId() {
        return Long.valueOf(StpUtil.getLoginId().toString());
    }

    @Override
    public User userGetCurrentLoginUser() {
        Long loginUserId = this.userGetCurrentLonginUserId();
//        return this.userGetSessionById(loginUserId); // 缓存是有些更新问题的(如果性能要求较高, 则可以考虑使用)
        return this.getById(loginUserId); // 最好是通过数据库查询实时更新, 否则某些场景是有问题的
    }

    @Override
    public User userIsExist(String userAccount) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(User::getUserAccount, userAccount)
        ;
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    public User userIsExist(String userAccount, String userName) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserName, userName)
        ;
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    public String encodePassword(String rawPassword) {
        if (!this.isPasswordValid(rawPassword)) {
            throw new IllegalArgumentException("密码长度必须为 8 到 72 个 UTF-8 字节");
        }
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    @Override
    public String encodePasswordForMigration(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()
                || rawPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("旧密码无法迁移，请重置密码");
        }
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    @Override
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        if (encodedPassword.startsWith("$2")) {
            try {
                return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }
        if (!this.needsPasswordUpgrade(encodedPassword)) {
            return false;
        }
        String legacyPassword = DigestUtils.md5DigestAsHex(
                (UserConstant.LEGACY_PASSWORD_SALT + rawPassword).getBytes(StandardCharsets.UTF_8)
        );
        return MessageDigest.isEqual(
                legacyPassword.getBytes(StandardCharsets.US_ASCII),
                encodedPassword.toLowerCase().getBytes(StandardCharsets.US_ASCII)
        );
    }

    @Override
    public boolean needsPasswordUpgrade(String encodedPassword) {
        return encodedPassword != null && encodedPassword.matches("(?i)^[0-9a-f]{32}$");
    }

    @Override
    public String generateTemporaryPassword() {
        List<Character> characters = new ArrayList<>(TEMPORARY_PASSWORD_LENGTH);
        characters.add(randomCharacter(UPPERCASE));
        characters.add(randomCharacter(LOWERCASE));
        characters.add(randomCharacter(DIGITS));
        characters.add(randomCharacter(SPECIALS));
        while (characters.size() < TEMPORARY_PASSWORD_LENGTH) {
            characters.add(randomCharacter(TEMPORARY_PASSWORD_ALPHABET));
        }
        Collections.shuffle(characters, SECURE_RANDOM);
        StringBuilder password = new StringBuilder(TEMPORARY_PASSWORD_LENGTH);
        for (Character character : characters) {
            password.append(character);
        }
        return password.toString();
    }

    @Override
    public boolean isPasswordValid(String rawPassword) {
        if (rawPassword == null) {
            return false;
        }
        int byteLength = rawPassword.getBytes(StandardCharsets.UTF_8).length;
        return byteLength >= 8 && byteLength <= 72;
    }

    private static char randomCharacter(String characters) {
        return characters.charAt(SECURE_RANDOM.nextInt(characters.length()));
    }

    @Override
    public Boolean userIsAdmin(User user) {
        return user != null && Objects.equals(user.getUserRole(), UserRoleEnum.ADMIN.getCode());
    }

    @Override
    public Boolean userIsDept(User user) {
        return user != null && Objects.equals(user.getUserRole(), UserRoleEnum.DEPT.getCode());
    }

    @Override
    public Boolean userIsTeacher(User user) {
        return user != null && Objects.equals(user.getUserRole(), UserRoleEnum.TEACHER.getCode());
    }

    @Override
    public Boolean userIsStudent(User user) {
        return user != null && Objects.equals(user.getUserRole(), UserRoleEnum.STUDENT.getCode());
    }

    // TODO: 下面代码可以迁移到 UserVO 中

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

}
