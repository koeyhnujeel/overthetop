package com.zoonza.overthetop.account.internal.domain;

import java.util.regex.Pattern;

public record ProfileName(String value) {
    private static final int MAX_LENGTH = 12;
    private static final Pattern FORMAT = Pattern.compile("[가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9]+");

    public ProfileName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("프로필 이름 값은 비어 있을 수 없습니다.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("프로필 이름은 1자 이상 12자 이하여야 합니다.");
        }
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("프로필 이름은 한글, 영어, 숫자만 사용할 수 있습니다.");
        }
    }
}
