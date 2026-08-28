package cn.com.edtechhub.worktopicselection.manager.bootstrap;

import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.model.enums.UserRoleEnum;
import cn.com.edtechhub.worktopicselection.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminBootstrapTest {

    @Test
    void blankConfigurationDoesNothing() {
        UserService userService = mock(UserService.class);
        AdminBootstrap bootstrap = bootstrap(userService, "", "", "");

        bootstrap.run();

        verify(userService, never()).save(any(User.class));
    }

    @Test
    void configuredBootstrapCreatesInitialAdminWithEncodedPassword() {
        UserService userService = mock(UserService.class);
        when(userService.isPasswordValid("Temporary-Admin-9!")).thenReturn(true);
        when(userService.encodePassword("Temporary-Admin-9!")).thenReturn("bcrypt-hash");
        when(userService.save(any(User.class))).thenReturn(true);
        AdminBootstrap bootstrap = bootstrap(
                userService,
                " admin001 ",
                " 本地管理员 ",
                "Temporary-Admin-9!"
        );

        bootstrap.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(captor.capture());
        User administrator = captor.getValue();
        assertEquals("admin001", administrator.getUserAccount());
        assertEquals("本地管理员", administrator.getUserName());
        assertEquals("bcrypt-hash", administrator.getUserPassword());
        assertEquals(UserRoleEnum.ADMIN.getCode(), administrator.getUserRole());
        assertNull(administrator.getStatus());
    }

    @Test
    void configuredBootstrapDoesNothingWhenAnyAdminAlreadyExists() {
        UserService userService = mock(UserService.class);
        when(userService.isPasswordValid("Temporary-Admin-9!")).thenReturn(true);
        when(userService.count(any())).thenReturn(1L);
        AdminBootstrap bootstrap = bootstrap(
                userService,
                "admin002",
                "另一个管理员",
                "Temporary-Admin-9!"
        );

        bootstrap.run();

        verify(userService, never()).save(any(User.class));
    }

    private static AdminBootstrap bootstrap(
            UserService userService,
            String account,
            String name,
            String password
    ) {
        AdminBootstrap bootstrap = new AdminBootstrap();
        ReflectionTestUtils.setField(bootstrap, "userService", userService);
        ReflectionTestUtils.setField(bootstrap, "account", account);
        ReflectionTestUtils.setField(bootstrap, "name", name);
        ReflectionTestUtils.setField(bootstrap, "password", password);
        return bootstrap;
    }
}
