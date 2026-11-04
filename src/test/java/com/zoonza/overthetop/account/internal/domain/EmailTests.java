package com.zoonza.overthetop.account.internal.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class EmailTests {

    @ParameterizedTest
    @ValueSource(strings = {"member@example.com", "First.Last+tag@sub.example.co.kr"})
    void 유효한_이메일은_대소문자를_포함한_입력값을_그대로_보존한다(String value) {
        assertThat(new Email(value).value()).isEqualTo(value);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ", "member", "member@", "@example.com", "member@@example.com",
            "member@example", " member@example.com", "member@example.com ",
            "mem ber@example.com", ".member@example.com", "member.@example.com",
            "first..last@example.com", "member@-example.com", "member@example-.com",
            "member@example..com", "member@exam_ple.com"
    })
    void 빈_값이나_형식이_잘못된_이메일을_거부한다(String value) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Email(value));
    }

    @Test
    void 이메일은_최대_길이인_254자까지_허용한다() {
        String value = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63)
                + "." + "d".repeat(61);

        assertThat(new Email(value).value()).isEqualTo(value);
    }

    @Test
    void 길이가_254자를_초과하는_이메일을_거부한다() {
        String value = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63)
                + "." + "d".repeat(62);

        assertThatIllegalArgumentException().isThrownBy(() -> new Email(value));
    }

    @Test
    void 로컬_부분이_64자를_초과하는_이메일을_거부한다() {
        String value = "a".repeat(65) + "@example.com";

        assertThatIllegalArgumentException().isThrownBy(() -> new Email(value));
    }

    @Test
    void 도메인_라벨이_63자를_초과하는_이메일을_거부한다() {
        String value = "member@" + "a".repeat(64) + ".com";

        assertThatIllegalArgumentException().isThrownBy(() -> new Email(value));
    }

    @Test
    void 이메일의_동등성과_해시_코드는_값으로_결정한다() {
        Email email = new Email("member@example.com");

        assertThat(email).isEqualTo(new Email("member@example.com"))
                .hasSameHashCodeAs(new Email("member@example.com"))
                .isNotEqualTo(new Email("other@example.com"));
    }
}
