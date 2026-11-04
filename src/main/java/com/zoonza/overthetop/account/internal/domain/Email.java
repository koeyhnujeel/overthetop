package com.zoonza.overthetop.account.internal.domain;

import java.util.regex.Pattern;

public record Email(String value) {
    private static final int MAX_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final String LOCAL_ATOM = "[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+";
    private static final String DOMAIN_LABEL = "[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?";
    private static final Pattern FORMAT = Pattern.compile(
            LOCAL_ATOM + "(?:\\." + LOCAL_ATOM + ")*@"
                    + DOMAIN_LABEL + "(?:\\." + DOMAIN_LABEL + ")+"
    );

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일 값은 비어 있을 수 없습니다.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("이메일은 254자 이하여야 합니다.");
        }
        if (!FORMAT.matcher(value).matches() || value.indexOf('@') > MAX_LOCAL_PART_LENGTH) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
    }
}
