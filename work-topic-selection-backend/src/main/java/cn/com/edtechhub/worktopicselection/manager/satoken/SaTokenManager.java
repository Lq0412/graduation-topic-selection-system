package cn.com.edtechhub.worktopicselection.manager.satoken;

import cn.com.edtechhub.worktopicselection.constant.UserConstant;
import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.model.enums.UserRoleEnum;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Sa-token 管理器
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Component
@Slf4j
public class SaTokenManager implements StpInterface {

    public boolean isTokenValidForLoginId(String tokenValue, Long loginId) {
        if (tokenValue == null || loginId == null) {
            return false;
        }
        try {
            Object tokenLoginId = StpUtil.getLoginIdByToken(tokenValue);
            return tokenLoginId != null
                    && Objects.equals(String.valueOf(tokenLoginId), String.valueOf(loginId));
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 制作空的角色标识集合
        List<String> list = new ArrayList<>();

        // 获取当前登录用户信息
        User user = (User) StpUtil.getSessionByLoginId(loginId).get(UserConstant.USER_LOGIN_STATE); // 直接从会话缓存中获取用户的所有信息
        UserRoleEnum userRole = user == null || user.getUserRole() == null
                ? null
                : UserRoleEnum.getEnums(user.getUserRole()); // 由于在本数据库中为了拓展性使用数字来标识身份, 因此需要做一层转化

        // 返回角色标识集合
        if (userRole != null && userRole != UserRoleEnum.BAN_ROLE) {
            list.add(userRole.getDescription());
        }
        log.debug("本次调用用户携带的角色标识集合为: {}", list);
        return list;
    }

    /**
     * 返回一个账号所拥有的权限码值集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 制作空的权限码值集合
        List<String> list = new ArrayList<>();

        // 返回权限码值集合
        log.debug("本次调用用户携带的的权限码值集合为 {}", list);
        return list;
    }

}
