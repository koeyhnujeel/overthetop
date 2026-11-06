package com.zoonza.overthetop.account.internal.domain;

import com.zoonza.overthetop.account.internal.fixture.AccountFixture;
import com.zoonza.overthetop.account.internal.fixture.ProfileFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountTests {
    private static final Email EMAIL = new Email("member@example.com");
    private static final Instant NOW = AccountFixture.SAVED_AT.plusSeconds(60);

    @Nested
    class 가입 {

        private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        @Test
        void 계정을_생성한다() {
            when(passwordEncoder.encode("password")).thenReturn("encoded-password");

            Account account = Account.register(EMAIL, "password", NOW, passwordEncoder);

            assertThat(account.getEmail()).isEqualTo(EMAIL);
            assertThat(account.getEncodedPassword()).isEqualTo("encoded-password");
            assertThat(account.getProfiles()).isEmpty();
            assertThat(account.getCreatedAt()).isEqualTo(NOW);
            assertThat(account.getUpdatedAt()).isEqualTo(NOW);
        }

        @Test
        void 이메일이_없으면_가입할_수_없다() {
            when(passwordEncoder.encode("password")).thenReturn("encoded-password");

            assertThatNullPointerException()
                    .isThrownBy(() -> Account.register(null, "password", NOW, passwordEncoder))
                    .withMessage("계정 이메일은 필수입니다.");
        }

        @Test
        void 비밀번호_인코딩_결과가_null이면_가입에_실패한다() {
            when(passwordEncoder.encode("password")).thenReturn(null);

            assertThatNullPointerException()
                    .isThrownBy(() -> Account.register(EMAIL, "password", NOW, passwordEncoder))
                    .withMessage("계정 비밀번호는 필수입니다.");
        }
    }

    @Nested
    class 비밀번호_확인 {

        private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void 인코더의_비밀번호_비교_결과를_반환한다(boolean matches) {
            Account account = AccountFixture.savedAccount();

            when(passwordEncoder.matches("input", AccountFixture.ENCODED_PASSWORD)).thenReturn(matches);

            assertThat(account.verifyPassword("input", passwordEncoder)).isEqualTo(matches);
            assertThat(account.getEncodedPassword()).isEqualTo(AccountFixture.ENCODED_PASSWORD);
            assertThat(account.getUpdatedAt()).isEqualTo(AccountFixture.SAVED_AT);
        }
    }

    @Nested
    class 비밀번호_변경 {

        private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        @Test
        void 비밀번호를_변경한다() {
            Account account = AccountFixture.savedAccount();

            when(passwordEncoder.encode("new-password")).thenReturn("new-encoded-password");

            account.changePassword("new-password", passwordEncoder);

            assertThat(account.getEncodedPassword()).isEqualTo("new-encoded-password");
            assertThat(account.getUpdatedAt()).isAfter(AccountFixture.SAVED_AT);
            assertThat(account.getCreatedAt()).isEqualTo(AccountFixture.SAVED_AT);
        }

        @Test
        void 인코딩에_실패하면_비밀번호가_바뀌지_않는다() {
            Account account = AccountFixture.savedAccount();

            when(passwordEncoder.encode("new-password"))
                    .thenThrow(new IllegalArgumentException("인코딩 실패"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> account.changePassword("new-password", passwordEncoder))
                    .withMessage("인코딩 실패");

            assertThat(account.getEncodedPassword()).isEqualTo(AccountFixture.ENCODED_PASSWORD);
            assertThat(account.getUpdatedAt()).isEqualTo(AccountFixture.SAVED_AT);
        }
    }

    @Nested
    class 프로필_추가 {

        @Test
        void 프로필을_추가한다() {
            Account account = AccountFixture.savedAccount();
            ProfileName name = new ProfileName("프로필1");

            account.addProfile(name, "icon-1", NOW);

            assertThat(account.getProfiles()).singleElement().satisfies(profile -> {
                assertThat(profile.getName()).isEqualTo(name);
                assertThat(profile.getIconRef()).isEqualTo("icon-1");
                assertThat(profile.getCreatedAt()).isEqualTo(NOW);
                assertThat(profile.getUpdatedAt()).isEqualTo(NOW);
            });
            assertThat(account.getUpdatedAt()).isEqualTo(NOW);
            assertThat(account.getCreatedAt()).isEqualTo(AccountFixture.SAVED_AT);
        }

        @Test
        void 프로필을_5개까지_추가할_수_있다() {
            Account account = AccountFixture.savedAccount();

            for (int i = 1; i <= 5; i++) {
                account.addProfile(new ProfileName("프로필" + i), "icon-" + i, NOW);
            }

            assertThat(account.getProfiles()).hasSize(5);
        }

        @Test
        void 프로필이_5개이면_더_추가할_수_없다() {
            Account account = AccountFixture.savedAccount();

            for (int i = 1; i <= 5; i++) {
                account.addProfile(new ProfileName("프로필" + i), "icon-" + i, NOW);
            }

            List<Profile> profilesBefore = List.copyOf(account.getProfiles());
            Instant updatedAtBefore = account.getUpdatedAt();

            assertThatIllegalStateException()
                    .isThrownBy(() -> account.addProfile(new ProfileName("프로필6"), "icon-6", NOW.plusSeconds(1)))
                    .withMessage("프로필은 최대 5개까지 생성할 수 있습니다.");

            assertThat(account.getProfiles()).containsExactlyElementsOf(profilesBefore);
            assertThat(account.getUpdatedAt()).isEqualTo(updatedAtBefore);
        }

        @Test
        void 같은_이름의_프로필을_추가할_수_없다() {
            Profile original = ProfileFixture.savedProfile(1000L, "프로필1");
            Account account = AccountFixture.savedAccount(original);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> account.addProfile(new ProfileName("프로필1"), "icon-2", NOW))
                    .withMessage("이미 사용 중인 프로필 이름입니다.");

            assertThat(account.getProfiles()).containsExactly(original);
            assertThat(account.getUpdatedAt()).isEqualTo(AccountFixture.SAVED_AT);
        }
    }

    @Nested
    class 프로필_이름_변경 {

        @Test
        void 프로필_이름을_변경한다() {
            Profile first = ProfileFixture.savedProfile(1000L, "프로필1");
            Profile second = ProfileFixture.savedProfile(2000L, "프로필2");
            Account account = AccountFixture.savedAccount(first, second);
            ProfileName newName = new ProfileName("새이름");

            account.renameProfile(2000L, newName, NOW);

            assertThat(second.getName()).isEqualTo(newName);
            assertThat(second.getUpdatedAt()).isEqualTo(NOW);
            assertThat(first.getName()).isEqualTo(new ProfileName("프로필1"));
            assertThat(first.getUpdatedAt()).isEqualTo(ProfileFixture.SAVED_AT);
            assertThat(account.getUpdatedAt()).isEqualTo(AccountFixture.SAVED_AT);
        }

        @Test
        void 같은_이름으로_변경하면_아무것도_바뀌지_않는다() {
            Profile profile = ProfileFixture.savedProfile(1000L, "프로필1");
            Account account = AccountFixture.savedAccount(profile);

            account.renameProfile(1000L, new ProfileName("프로필1"), NOW);

            assertThat(profile.getName()).isEqualTo(new ProfileName("프로필1"));
            assertThat(profile.getUpdatedAt()).isEqualTo(ProfileFixture.SAVED_AT);
            assertThat(account.getUpdatedAt()).isEqualTo(AccountFixture.SAVED_AT);
        }

        @Test
        void 다른_프로필과_같은_이름으로_변경할_수_없다() {
            Profile first = ProfileFixture.savedProfile(1000L, "프로필1");
            Profile second = ProfileFixture.savedProfile(2000L, "프로필2");
            Account account = AccountFixture.savedAccount(first, second);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> account.renameProfile(1000L, new ProfileName("프로필2"), NOW))
                    .withMessage("이미 사용 중인 프로필 이름입니다.");

            assertThat(first.getName()).isEqualTo(new ProfileName("프로필1"));
            assertThat(second.getName()).isEqualTo(new ProfileName("프로필2"));
            assertThat(first.getUpdatedAt()).isEqualTo(ProfileFixture.SAVED_AT);
            assertThat(second.getUpdatedAt()).isEqualTo(ProfileFixture.SAVED_AT);
            assertThat(account.getUpdatedAt()).isEqualTo(AccountFixture.SAVED_AT);
        }

        @Test
        void 존재하지_않는_프로필의_이름을_변경할_수_없다() {
            Profile profile = ProfileFixture.savedProfile(1000L, "프로필1");
            Account account = AccountFixture.savedAccount(profile);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> account.renameProfile(2000L, new ProfileName("새이름"), NOW))
                    .withMessage("프로필을 찾을 수 없습니다.");

            assertThat(profile.getName()).isEqualTo(new ProfileName("프로필1"));
            assertThat(profile.getUpdatedAt()).isEqualTo(ProfileFixture.SAVED_AT);
            assertThat(account.getUpdatedAt()).isEqualTo(AccountFixture.SAVED_AT);
        }
    }

    @Nested
    class 프로필_아이콘_변경 {

        @Test
        void 프로필_아이콘을_변경한다() {
            Profile first = ProfileFixture.savedProfile(1000L, "프로필1");
            Profile second = ProfileFixture.savedProfile(2000L, "프로필2");
            Account account = AccountFixture.savedAccount(first, second);

            account.changeProfileIcon(2000L, "new-icon", NOW);

            assertThat(second.getIconRef()).isEqualTo("new-icon");
            assertThat(second.getUpdatedAt()).isEqualTo(NOW);
            assertThat(first.getIconRef()).isEqualTo("icon-1000");
            assertThat(first.getUpdatedAt()).isEqualTo(ProfileFixture.SAVED_AT);
            assertThat(account.getUpdatedAt()).isEqualTo(AccountFixture.SAVED_AT);
        }

        @Test
        void 존재하지_않는_프로필의_아이콘을_변경할_수_없다() {
            Profile profile = ProfileFixture.savedProfile(1000L, "프로필1");
            Account account = AccountFixture.savedAccount(profile);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> account.changeProfileIcon(2000L, "new-icon", NOW))
                    .withMessage("프로필을 찾을 수 없습니다.");

            assertThat(profile.getIconRef()).isEqualTo("icon-1000");
            assertThat(profile.getUpdatedAt()).isEqualTo(ProfileFixture.SAVED_AT);
            assertThat(account.getUpdatedAt()).isEqualTo(AccountFixture.SAVED_AT);
        }
    }

    @Nested
    class 프로필_삭제 {

        @Test
        void 프로필을_삭제한다() {
            Profile first = ProfileFixture.savedProfile(1000L, "프로필1");
            Profile second = ProfileFixture.savedProfile(2000L, "프로필2");
            Account account = AccountFixture.savedAccount(first, second);

            account.removeProfile(2000L, NOW);

            assertThat(account.getProfiles()).containsExactly(first);
            assertThat(account.getUpdatedAt()).isEqualTo(NOW);
            assertThat(account.getCreatedAt()).isEqualTo(AccountFixture.SAVED_AT);
            assertThat(first.getUpdatedAt()).isEqualTo(ProfileFixture.SAVED_AT);
        }

        @Test
        void 존재하지_않는_프로필을_삭제하면_아무것도_바뀌지_않는다() {
            Profile profile = ProfileFixture.savedProfile(1000L, "프로필1");
            Account account = AccountFixture.savedAccount(profile);

            account.removeProfile(2000L, NOW);

            assertThat(account.getProfiles()).containsExactly(profile);
            assertThat(account.getUpdatedAt()).isEqualTo(AccountFixture.SAVED_AT);
        }

        @Test
        void 이미_삭제한_프로필을_다시_삭제하면_아무것도_바뀌지_않는다() {
            Profile profile = ProfileFixture.savedProfile(1000L, "프로필1");
            Account account = AccountFixture.savedAccount(profile);
            account.removeProfile(1000L, NOW);
            Instant updatedAtAfterRemoval = account.getUpdatedAt();

            account.removeProfile(1000L, NOW.plusSeconds(1));

            assertThat(account.getProfiles()).isEmpty();
            assertThat(account.getUpdatedAt()).isEqualTo(updatedAtAfterRemoval);
        }

        @Test
        void 삭제한_프로필의_이름을_다시_사용할_수_있다() {
            Profile profile = ProfileFixture.savedProfile(1000L, "프로필1");
            Account account = AccountFixture.savedAccount(profile);
            account.removeProfile(1000L, NOW);

            account.addProfile(new ProfileName("프로필1"), "new-icon", NOW.plusSeconds(1));

            assertThat(account.getProfiles()).singleElement().satisfies(addedProfile -> {
                assertThat(addedProfile.getName()).isEqualTo(new ProfileName("프로필1"));
                assertThat(addedProfile.getIconRef()).isEqualTo("new-icon");
            });
        }
    }
}
