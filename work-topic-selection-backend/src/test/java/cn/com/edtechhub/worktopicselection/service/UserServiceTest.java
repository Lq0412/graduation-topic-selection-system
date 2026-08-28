package cn.com.edtechhub.worktopicselection.service;

import cn.com.edtechhub.worktopicselection.constant.UserConstant;
import cn.com.edtechhub.worktopicselection.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    private final UserService userService = new UserServiceImpl();

    @Test
    void encodesNewPasswordsWithSaltedBcrypt() {
        String rawPassword = "Correct-Horse-7!";

        String firstHash = userService.encodePassword(rawPassword);
        String secondHash = userService.encodePassword(rawPassword);

        assertTrue(firstHash.startsWith("$2"));
        assertNotEquals(firstHash, secondHash);
        assertTrue(userService.matchesPassword(rawPassword, firstHash));
        assertFalse(userService.matchesPassword("wrong-password", firstHash));
        assertFalse(userService.needsPasswordUpgrade(firstHash));
    }

    @Test
    void acceptsLegacyMd5OnlyForMigration() {
        String rawPassword = "legacy-password";
        String legacyHash = DigestUtils.md5DigestAsHex(
                (UserConstant.LEGACY_PASSWORD_SALT + rawPassword).getBytes(StandardCharsets.UTF_8)
        );

        assertTrue(userService.needsPasswordUpgrade(legacyHash));
        assertTrue(userService.matchesPassword(rawPassword, legacyHash));
        assertFalse(userService.matchesPassword("wrong-password", legacyHash));

        String upgradedHash = userService.encodePassword(rawPassword);
        assertTrue(userService.matchesPassword(rawPassword, upgradedHash));
        assertFalse(userService.needsPasswordUpgrade(upgradedHash));
    }

    @Test
    void migratesLegacyPasswordThatPredatesCurrentMinimumLength() {
        String upgradedHash = userService.encodePasswordForMigration("short1");

        assertTrue(userService.matchesPassword("short1", upgradedHash));
        assertFalse(userService.isPasswordValid("short1"));
    }

    @Test
    void rejectsMalformedStoredPasswords() {
        assertFalse(userService.matchesPassword("password", null));
        assertFalse(userService.matchesPassword("password", "not-a-password-hash"));
        assertFalse(userService.matchesPassword("password", "$2broken"));
    }

    @Test
    void generatesUniqueStrongTemporaryPasswords() {
        Set<String> passwords = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            String password = userService.generateTemporaryPassword();
            assertTrue(userService.isPasswordValid(password));
            assertTrue(password.length() >= 12);
            assertTrue(password.matches(".*[A-Z].*"));
            assertTrue(password.matches(".*[a-z].*"));
            assertTrue(password.matches(".*[0-9].*"));
            assertTrue(password.matches(".*[!@#$%*\\-_].*"));
            assertTrue(passwords.add(password));
        }
    }

    @Test
    void enforcesBcryptInputLengthInUtf8Bytes() {
        assertFalse(userService.isPasswordValid("1234567"));
        assertTrue(userService.isPasswordValid(repeat('a', 72)));
        assertFalse(userService.isPasswordValid(repeat('a', 73)));
        assertFalse(userService.isPasswordValid(repeat('密', 25)));
    }

    private static String repeat(char character, int count) {
        StringBuilder value = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            value.append(character);
        }
        return value.toString();
    }
}
