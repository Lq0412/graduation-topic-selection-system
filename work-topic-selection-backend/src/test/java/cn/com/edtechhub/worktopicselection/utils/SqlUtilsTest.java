package cn.com.edtechhub.worktopicselection.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class SqlUtilsTest {

    // === validSortField ===
    /**
     * 场景：空字符串 → 返回 false
     */
    @Test
    void validSortField_givenEmptyString_returnsFalse() {
        boolean result = SqlUtils.validSortField("");
        assertFalse(result);
    }

    /**
     * 场景：null 值 → 返回 false
     */
    @Test
    void validSortField_givenNull_returnsFalse() {
        boolean result = SqlUtils.validSortField(null);
        assertFalse(result);
    }

    /**
     * 场景：纯空格字符串 → 返回 false
     */
    @Test
    void validSortField_givenBlankString_returnsFalse() {
        boolean result = SqlUtils.validSortField("   ");
        assertFalse(result);
    }

    /**
     * 场景：参数化验证 - 合法/非法排序字段
     * CsvSource 格式：输入值, 预期结果
     */
    @ParameterizedTest
    @CsvSource({
            // 合法场景
            "id, true",
            "userName, true",
            "createTime, true",
            // 非法场景（不在固定白名单中）
            "order123, false",
            "user_name, false",
            "id=1, false",
            "(id), false",
            "id), false",
            "id (, false",
            "id name, false", // 含空格
            "name=desc, false",
            "123(456), false"
    })
    void validSortField_givenVariousInputs_returnsExpectedResult(String sortField, boolean expected) {
        boolean result = SqlUtils.validSortField(sortField);
        assertEquals(expected, result, "输入：" + sortField + " 时结果不符合预期");
    }

    @ParameterizedTest
    @CsvSource({
            "topic, true",
            "surplusQuantity, true",
            "userPassword, false",
            "id desc; drop table topic, false",
            "id/**/desc, false"
    })
    void validTopicSortField_givenWhitelistAndAttackPayloads_returnsExpectedResult(String sortField, boolean expected) {
        assertEquals(expected, SqlUtils.validTopicSortField(sortField));
    }

    @ParameterizedTest
    @CsvSource({
            "ascend, true",
            "descend, true",
            "asc, false",
            "desc, false",
            "desc; drop table user, false",
            "'descend, nulls first', false"
    })
    void validSortOrder_givenWhitelistAndAttackPayloads_returnsExpectedResult(String sortOrder, boolean expected) {
        assertEquals(expected, SqlUtils.validSortOrder(sortOrder));
    }

}
