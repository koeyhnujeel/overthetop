package com.zoonza.overthetop.account.internal.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Getter
public class Account {
    private static final int MAX_PROFILE_COUNT = 5;

    private Long id;
    private Email email;
    private String encodedPassword;
    private Instant createdAt;
    private Instant updatedAt;
    private List<Profile> profiles = new ArrayList<>();

    private Account(
            Email email,
            String encodedPassword,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.email = requireNonNull(email, "계정 이메일은 필수입니다.");
        this.encodedPassword = requireNonNull(encodedPassword, "계정 비밀번호는 필수입니다.");
        this.createdAt = requireNonNull(createdAt, "계정 생성 시각은 필수입니다.");
        this.updatedAt = requireNonNull(updatedAt, "계정 수정 시각은 필수입니다.");
    }

    public static Account register(
            Email email,
            String rawPassword,
            PasswordEncoder passwordEncoder
    ) {
        String passwordHash = passwordEncoder.encode(rawPassword);
        Instant now = Instant.now();

        return new Account(email, passwordHash, now, now);
    }

    public boolean verifyPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.encodedPassword);
    }

    public void changePassword(String newPassword, PasswordEncoder passwordEncoder) {
        this.encodedPassword = passwordEncoder.encode(newPassword);
        this.updatedAt = Instant.now();
    }

    public void addProfile(ProfileName profileName, String iconRef) {
        if (this.profiles.size() >= MAX_PROFILE_COUNT) {
            throw new IllegalStateException("프로필은 최대 5개까지 생성할 수 있습니다.");
        }

        validateUniqueProfileName(profileName);

        Profile newProfile = Profile.create(profileName,  iconRef);

        this.profiles.add(newProfile);
        this.updatedAt = Instant.now();
    }

    public void renameProfile(Long profileId, ProfileName newName) {
        Profile profile = requireProfile(profileId);

        if (profile.matchesName(newName)) {
            return;
        }

        validateUniqueProfileName(newName);

        profile.rename(newName);
    }

    public void changeProfileIcon(Long profileId, String newIconRef) {
        Profile profile = requireProfile(profileId);

        profile.changeIcon(newIconRef);
    }

    public void removeProfile(Long profileId) {
        boolean removed = this.profiles.removeIf(profile -> profile.matchesId(profileId));

        if (removed) {
            this.updatedAt = Instant.now();
        }
    }

    private Profile requireProfile(Long profileId) {
        return this.profiles.stream()
                .filter(profile -> profile.matchesId(profileId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));
    }

    private void validateUniqueProfileName(ProfileName name) {
        boolean duplicated = this.profiles.stream()
                .anyMatch(profile -> profile.matchesName(name));

        if (duplicated) {
            throw new IllegalArgumentException("이미 사용 중인 프로필 이름입니다.");
        }
    }
}
