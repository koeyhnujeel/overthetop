package com.zoonza.overthetop.account.internal.domain;

import com.zoonza.overthetop.account.internal.fixture.ProfileFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ProfileTests {
    private static final Instant NOW = ProfileFixture.SAVED_AT.plusSeconds(60);

    @Test
    void 프로필을_생성한다() {
        ProfileName name = new ProfileName("프로필1");

        Profile profile = Profile.create(name, "icon-1", NOW);

        assertThat(profile.getName()).isEqualTo(name);
        assertThat(profile.getIconRef()).isEqualTo("icon-1");
        assertThat(profile.getCreatedAt()).isEqualTo(NOW);
        assertThat(profile.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void 이름_없이_프로필을_생성할_수_없다() {
        assertThatNullPointerException()
                .isThrownBy(() -> Profile.create(null, "icon-1", NOW))
                .withMessage("프로필 이름은 필수입니다.");
    }

    @Test
    void 아이콘_없이_프로필을_생성할_수_없다() {
        assertThatNullPointerException()
                .isThrownBy(() -> Profile.create(new ProfileName("프로필1"), null, NOW))
                .withMessage("프로필 아이콘은 필수입니다.");
    }

    @Test
    void 같은_ID의_프로필인지_확인한다() {
        Profile profile = ProfileFixture.savedProfile(Long.valueOf("1000"), "프로필1");

        assertThat(profile.matchesId(Long.valueOf("1000"))).isTrue();
    }

    @NullSource
    @ParameterizedTest
    @ValueSource(longs = {2000L})
    void 다른_ID는_일치하지_않는다(Long id) {
        Profile profile = ProfileFixture.savedProfile(1000L, "프로필1");

        assertThat(profile.matchesId(id)).isFalse();
    }

    @Test
    void ID가_없는_프로필은_값이_있는_ID와_일치하지_않는다() {
        Profile profile = Profile.create(new ProfileName("프로필1"), "icon-1", NOW);

        assertThat(profile.matchesId(1000L)).isFalse();
    }

    @Test
    void 같은_이름의_프로필인지_확인한다() {
        Profile profile = Profile.create(new ProfileName("프로필1"), "icon-1", NOW);

        assertThat(profile.matchesName(new ProfileName("프로필1"))).isTrue();
        assertThat(profile.matchesName(new ProfileName("프로필2"))).isFalse();
        assertThat(profile.matchesName(null)).isFalse();
    }

    @Test
    void 프로필_이름을_변경한다() {
        Profile profile = ProfileFixture.savedProfile(1000L, "프로필1");
        ProfileName newName = new ProfileName("새이름");

        profile.rename(newName, NOW);

        assertThat(profile.getName()).isEqualTo(newName);
        assertThat(profile.getUpdatedAt()).isEqualTo(NOW);
        assertThat(profile.getCreatedAt()).isEqualTo(ProfileFixture.SAVED_AT);
        assertThat(profile.getIconRef()).isEqualTo("icon-1000");
    }

    @Test
    void 프로필_아이콘을_변경한다() {
        Profile profile = ProfileFixture.savedProfile(1000L, "프로필1");

        profile.changeIcon("new-icon", NOW);

        assertThat(profile.getIconRef()).isEqualTo("new-icon");
        assertThat(profile.getUpdatedAt()).isEqualTo(NOW);
        assertThat(profile.getCreatedAt()).isEqualTo(ProfileFixture.SAVED_AT);
        assertThat(profile.getName()).isEqualTo(new ProfileName("프로필1"));
    }
}
