package com.zoonza.overthetop.account.internal.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ProfileNameTests {

    @ParameterizedTest
    @ValueSource(strings = {"가", "A", "0", "프로필이름", "Player123", "한글Ab12", "ㄱㄴㅏㅣ", "가나다라마바사아자차카타"})
    void 프로필_이름은_한글_영어_숫자로_이루어진_1자부터_12자까지_허용한다(String value) {
        assertThat(new ProfileName(value).value()).isEqualTo(value);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void null이나_빈_문자열_또는_공백으로만_된_프로필_이름을_거부한다(String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ProfileName(value))
                .withMessage("프로필 이름 값은 비어 있을 수 없습니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcdefghijklm", "가나다라마바사아자차카타파"})
    void 길이가_12자를_초과하는_프로필_이름을_거부한다(String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ProfileName(value))
                .withMessage("프로필 이름은 1자 이상 12자 이하여야 합니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"이름!", "name_1", "name-1", "이 름", " name", "name ", "이름\n", "이름😀", "名前", "café", "１２３"})
    void 한글_영어_숫자_이외의_문자가_포함된_프로필_이름을_거부한다(String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ProfileName(value))
                .withMessage("프로필 이름은 한글, 영어, 숫자만 사용할 수 있습니다.");
    }
}
