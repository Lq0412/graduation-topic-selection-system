package cn.com.edtechhub.worktopicselection.controller;

import cn.com.edtechhub.worktopicselection.constant.UserConstant;
import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.manager.redis.RedisManager;
import cn.com.edtechhub.worktopicselection.manager.sentine.SentineManager;
import cn.com.edtechhub.worktopicselection.model.dto.user.CaptchaRequest;
import cn.com.edtechhub.worktopicselection.model.dto.user.SendCodeRequest;
import cn.com.edtechhub.worktopicselection.model.dto.user.UserLoginRequest;
import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.model.enums.UserRoleEnum;
import cn.com.edtechhub.worktopicselection.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class UserControllerAuthTest {

    @Test
    void roleToggleOnlyAllowsTeacherAndDeptPair() {
        assertTrue(UserController.isAllowedRoleToggle(
                UserRoleEnum.TEACHER.getCode(), UserRoleEnum.DEPT.getCode()
        ));
        assertTrue(UserController.isAllowedRoleToggle(
                UserRoleEnum.DEPT.getCode(), UserRoleEnum.TEACHER.getCode()
        ));
        assertFalse(UserController.isAllowedRoleToggle(
                UserRoleEnum.TEACHER.getCode(), UserRoleEnum.ADMIN.getCode()
        ));
        assertFalse(UserController.isAllowedRoleToggle(
                UserRoleEnum.DEPT.getCode(), UserRoleEnum.STUDENT.getCode()
        ));
    }

    @Test
    void passwordResetMailIsRateLimitedByNormalizedAccount() {
        UserController controller = controllerWithRateLimiter(
                "password-reset-rate:student01",
                false
        );
        SendCodeRequest request = new SendCodeRequest();
        request.setUserAccount(" Student01 ");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> controller.sendCode(request, remoteRequest("203.0.113.10"))
        );

        assertSame(CodeBindMessageEnums.FLOW_RULES, exception.getCodeBindMessageEnums());
        verify(controller.redisManager).tryAcquire("password-reset-rate:student01", 1, 60);
    }

    @Test
    void captchaMailIsRateLimitedByNormalizedEmail() {
        UserController controller = controllerWithRateLimiter(
                "email-captcha-rate:owner@example.com",
                false
        );
        CaptchaRequest request = new CaptchaRequest();
        request.setEmail(" Owner@Example.com ");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> controller.sendCaptcha(request, remoteRequest("203.0.113.10"))
        );

        assertSame(CodeBindMessageEnums.FLOW_RULES, exception.getCodeBindMessageEnums());
        verify(controller.redisManager).tryAcquire("email-captcha-rate:owner@example.com", 1, 60);
    }

    @Test
    void loginRateLimitUsesAccountAndDirectRemoteAddress() {
        UserController controller = new UserController();
        controller.redisManager = mock(RedisManager.class);
        when(controller.redisManager.tryAcquire("login-account-rate:student01", 8, 300)).thenReturn(true);
        when(controller.redisManager.tryAcquire("login-ip-rate:203.0.113.10", 300, 300)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> controller.enforceLoginRateLimit("Student01", remoteRequest("203.0.113.10"))
        );

        assertSame(CodeBindMessageEnums.FLOW_RULES, exception.getCodeBindMessageEnums());
        verify(controller.redisManager).releaseRateLimit("login-account-rate:student01");
    }

    @Test
    void successfulAuthenticationOnlyReleasesItsOwnReservedAttempt() {
        UserController controller = new UserController();
        controller.redisManager = mock(RedisManager.class);

        controller.releaseLoginAttempt("Student01", remoteRequest("203.0.113.10"));

        verify(controller.redisManager).releaseRateLimit("login-account-rate:student01");
        verify(controller.redisManager).releaseRateLimit("login-ip-rate:203.0.113.10");
    }

    @Test
    void mailRateLimitCannotBeBypassedByRotatingTargetsFromOneAddress() {
        UserController controller = new UserController();
        controller.redisManager = mock(RedisManager.class);
        when(controller.redisManager.tryAcquire("email-captcha-rate:new@example.com", 1, 60)).thenReturn(true);
        when(controller.redisManager.tryAcquire("mail-ip-rate:203.0.113.10", 100, 600)).thenReturn(false);
        when(controller.redisManager.tryAcquire("mail-global-rate", 200, 3600)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> controller.enforceMailRateLimit(
                        "email-captcha-rate:new@example.com",
                        remoteRequest("203.0.113.10"),
                        "发送过于频繁"
                )
        );

        assertSame(CodeBindMessageEnums.FLOW_RULES, exception.getCodeBindMessageEnums());
        verify(controller.redisManager).releaseRateLimit("email-captcha-rate:new@example.com");
        verify(controller.redisManager, never()).tryAcquire("mail-global-rate", 200, 3600);
    }

    @Test
    void databaseCanonicalAccountIsUsedForRateLimitKey() {
        User user = new User();
        user.setUserAccount("resume");

        assertEquals("resume", UserController.canonicalAccountForRateLimit("résumé", user));
    }

    @Test
    void bannedUserCannotCreateLoginSession() {
        UserController controller = new UserController();
        controller.sentineManager = mock(SentineManager.class);
        controller.redisManager = mock(RedisManager.class);
        UserService userService = mock(UserService.class);
        ReflectionTestUtils.setField(controller, "userService", userService);
        User bannedUser = new User();
        bannedUser.setId(99L);
        bannedUser.setUserAccount("banned");
        bannedUser.setUserPassword("hash");
        bannedUser.setUserRole(UserRoleEnum.BAN_ROLE.getCode());
        bannedUser.setStatus("老用户");
        when(controller.redisManager.tryAcquire("login-account-rate:banned", 8, 300)).thenReturn(true);
        when(controller.redisManager.tryAcquire("login-ip-rate:203.0.113.10", 300, 300)).thenReturn(true);
        when(userService.userIsExist("banned")).thenReturn(bannedUser);
        when(userService.matchesPassword("password", "hash")).thenReturn(true);
        UserLoginRequest request = new UserLoginRequest();
        request.setUserAccount("banned");
        request.setUserPassword("password");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> controller.userLogin(request, remoteRequest("203.0.113.10"))
        );

        assertSame(CodeBindMessageEnums.NO_AUTH_ERROR, exception.getCodeBindMessageEnums());
    }

    @Test
    void verifiedEmailIsBoundToSessionAndConsumedOnce() {
        MockHttpServletRequest request = verifiedEmailRequest("owner@example.com", System.currentTimeMillis() + 60_000);

        assertTrue(UserController.consumeVerifiedEmail(request, "owner@example.com"));
        assertFalse(UserController.consumeVerifiedEmail(request, "owner@example.com"));
    }

    @Test
    void wrongEmailConsumesProofWithoutAuthorizingBinding() {
        MockHttpServletRequest request = verifiedEmailRequest("owner@example.com", System.currentTimeMillis() + 60_000);

        assertFalse(UserController.consumeVerifiedEmail(request, "attacker@example.com"));
        assertFalse(UserController.consumeVerifiedEmail(request, "owner@example.com"));
    }

    @Test
    void expiredEmailProofCannotBeUsedAndIsRemoved() {
        MockHttpServletRequest request = verifiedEmailRequest("owner@example.com", System.currentTimeMillis() - 1);
        MockHttpSession session = (MockHttpSession) request.getSession(false);

        assertFalse(UserController.consumeVerifiedEmail(request, "owner@example.com"));
        assertNull(session.getAttribute(UserConstant.VERIFIED_EMAIL_SESSION_KEY));
        assertNull(session.getAttribute(UserConstant.VERIFIED_EMAIL_EXPIRES_AT_SESSION_KEY));
    }

    private static MockHttpServletRequest verifiedEmailRequest(String email, long expiresAt) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = (MockHttpSession) request.getSession(true);
        session.setAttribute(UserConstant.VERIFIED_EMAIL_SESSION_KEY, email);
        session.setAttribute(UserConstant.VERIFIED_EMAIL_EXPIRES_AT_SESSION_KEY, expiresAt);
        return request;
    }

    private static MockHttpServletRequest remoteRequest(String address) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(address);
        return request;
    }

    private static UserController controllerWithRateLimiter(String key, boolean acquired) {
        UserController controller = new UserController();
        controller.sentineManager = mock(SentineManager.class);
        controller.redisManager = mock(RedisManager.class);
        UserService userService = mock(UserService.class);
        ReflectionTestUtils.setField(controller, "userService", userService);
        when(userService.getOne(any())).thenReturn(null);
        when(controller.redisManager.tryAcquire(key, 1, 60)).thenReturn(acquired);
        return controller;
    }
}
