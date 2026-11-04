package com.zoonza.overthetop.account.internal.fixture;

import com.zoonza.overthetop.account.internal.domain.Account;
import com.zoonza.overthetop.account.internal.domain.Email;
import com.zoonza.overthetop.account.internal.domain.PasswordEncoder;
import com.zoonza.overthetop.account.internal.domain.Profile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AccountFixture {

    public static final Instant SAVED_AT = Instant.parse("2026-01-01T00:00:00Z");
    public static final String ENCODED_PASSWORD = "encoded-password";

    private AccountFixture() {
    }

    public static Account savedAccount(Profile... profiles) {
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode("password")).thenReturn(ENCODED_PASSWORD);

        Account account = Account.register(new Email("member@example.com"), "password", passwordEncoder);
        ReflectionTestUtils.setField(account, "id", 1L);
        ReflectionTestUtils.setField(account, "createdAt", SAVED_AT);
        ReflectionTestUtils.setField(account, "updatedAt", SAVED_AT);
        ReflectionTestUtils.setField(account, "profiles", new ArrayList<>(List.of(profiles)));
        return account;
    }
}
