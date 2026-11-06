package com.zoonza.overthetop.account.internal.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Getter
public class Profile {
    private Long id;
    private ProfileName name;
    private String iconRef;
    private Instant createdAt;
    private Instant updatedAt;

    private Profile(
            ProfileName name,
            String iconRef,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.name = requireNonNull(name, "프로필 이름은 필수입니다.");
        this.iconRef = requireNonNull(iconRef, "프로필 아이콘은 필수입니다.");
        this.createdAt = requireNonNull(createdAt, "프로필 생성 시각은 필수입니다.");
        this.updatedAt = requireNonNull(updatedAt, "프로필 수정 시각은 필수입니다.");
    }

    public static Profile create(ProfileName name, String iconRef, Instant createdAt) {
        return new Profile(name, iconRef, createdAt, createdAt);
    }

    public boolean matchesId(Long profileId) {
        return Objects.equals(this.id, profileId);
    }

    public boolean matchesName(ProfileName name) {
        return this.name.equals(name);
    }

    public void rename(ProfileName newName, Instant now) {
        this.name = newName;
        this.updatedAt = now;
    }

    public void changeIcon(String newIconRef, Instant now) {
        this.iconRef = newIconRef;
        this.updatedAt = now;
    }
}
