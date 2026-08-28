package cn.com.edtechhub.worktopicselection.model.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserSerializationTest {

    @Test
    void serializedUserDoesNotExposePasswordOrEmail() throws Exception {
        User user = new User();
        user.setUserAccount("student001");
        user.setUserPassword("bcrypt-hash");
        user.setEmail("student@example.com");

        String json = new ObjectMapper().writeValueAsString(user);

        assertTrue(json.contains("student001"));
        assertFalse(json.contains("userPassword"));
        assertFalse(json.contains("bcrypt-hash"));
        assertFalse(json.contains("email"));
        assertFalse(json.contains("student@example.com"));
    }

    @Test
    void toStringDoesNotExposePasswordOrEmail() {
        User user = new User();
        user.setUserPassword("bcrypt-hash");
        user.setEmail("student@example.com");

        String text = user.toString();

        assertFalse(text.contains("bcrypt-hash"));
        assertFalse(text.contains("student@example.com"));
    }
}
