package com.zoonza.overthetop.account.internal.fixture;

import com.zoonza.overthetop.account.internal.domain.Profile;
import com.zoonza.overthetop.account.internal.domain.ProfileName;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

public final class ProfileFixture {

    public static final Instant SAVED_AT = Instant.parse("2026-01-01T00:00:00Z");

    private ProfileFixture() {
    }

    public static Profile savedProfile(Long id, String name) {
        Profile profile = Profile.create(new ProfileName(name), "icon-" + id, SAVED_AT);
        ReflectionTestUtils.setField(profile, "id", id);
        return profile;
    }
}
