package com.cotato.itda.domain.member.entity;

import com.cotato.itda.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "member")
public class Member extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", length = 20, nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "birth_date")
    private String birthDate;

    @Column(name = "profile_name")
    private String profileName;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    public void completeOnboardingProfile(String profileImageUrl, String profileName) {
        this.profileImageUrl = profileImageUrl;
        this.profileName = profileName;
    }

    public void updateProfile(String profileImageUrl, String profileName, String birthDate) {
        this.profileImageUrl = profileImageUrl;
        this.profileName = profileName;
        this.birthDate = birthDate;
    }

    public void updateToDefaultProfileImage(String defaultProfileImageUrl) {
        this.profileImageUrl = defaultProfileImageUrl;
    }
}
