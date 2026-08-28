package cn.com.edtechhub.worktopicselection.manager.bootstrap;

import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.model.enums.UserRoleEnum;
import cn.com.edtechhub.worktopicselection.constant.UserConstant;
import cn.com.edtechhub.worktopicselection.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 可选的一次性管理员引导。未配置环境变量时不会执行任何操作。
 */
@Component
@Slf4j
public class AdminBootstrap implements CommandLineRunner {

    @Resource
    private UserService userService;

    @Value("${app.bootstrap-admin.account:}")
    private String account;

    @Value("${app.bootstrap-admin.name:}")
    private String name;

    @Value("${app.bootstrap-admin.password:}")
    private String password;

    @Override
    public void run(String... args) {
        if (StringUtils.isAllBlank(account, name, password)) {
            return;
        }
        if (StringUtils.isAnyBlank(account, name, password)) {
            throw new IllegalStateException("管理员引导账号、姓名和密码必须同时配置");
        }
        if (!userService.isPasswordValid(password)) {
            throw new IllegalStateException("管理员引导密码必须为 8 到 72 个 UTF-8 字节");
        }

        long administratorCount = userService.count(
                new QueryWrapper<User>().eq("userRole", UserRoleEnum.ADMIN.getCode())
        );
        if (administratorCount > 0) {
            log.info("系统已有管理员，跳过管理员引导");
            return;
        }

        String normalizedAccount = account.trim();
        if (normalizedAccount.length() > UserConstant.MAX_USER_ACCOUNT_LENGTH) {
            throw new IllegalStateException("管理员引导账号不能超过 128 个字符");
        }
        if (userService.userIsExist(normalizedAccount) != null) {
            throw new IllegalStateException("管理员引导账号已被非管理员用户占用");
        }

        User administrator = new User();
        administrator.setUserAccount(normalizedAccount);
        administrator.setUserName(name.trim());
        administrator.setUserPassword(userService.encodePassword(password));
        administrator.setUserRole(UserRoleEnum.ADMIN.getCode());
        administrator.setStatus(null);

        if (!userService.save(administrator)) {
            throw new IllegalStateException("创建管理员引导账号失败");
        }
        log.info("管理员引导账号已创建，首次登录前需要修改临时密码");
    }
}
