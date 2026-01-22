package com.cotato.itda.domain.member.entity;

import java.time.LocalDate;

import com.cotato.itda.global.entity.BaseTimeEntity;
import com.cotato.itda.global.error.constant.SignupErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(
	name = "members",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_members_phone_number", columnNames = "phone_number"),
		@UniqueConstraint(name = "uk_members_invite_code", columnNames = "invite_code")
	},
	indexes = {
		@Index(name = "idx_members_status", columnList = "status"),
		@Index(name = "idx_members_created_at", columnList = "created_at")
	}
)
public class Member extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "phone_number", nullable = false, length = 15)
	private String phoneNumber;

	// YYYY-MM-DD
	@Column(name = "birth_date", nullable = false, length = 10)
	private LocalDate birthDate;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Enumerated
	@Column(name = "role", nullable = false, length = 20)
	private MemberRole role;

	@Enumerated
	@Column(name = "status", nullable = false, length = 20)
	private MemberStatus status;

	@Column(name = "profile_image_url", length = 500)
	private String profileImageUrl;

    @Column(name = "profile_name", length = 100)
    private String profileName;

	@Column(name = "invite_code", length = 50)
	private String inviteCode;

	@Column(name = "password_hash", nullable = false, length = 200)
	private String passwordHash;

	@Column(name = "nutrient_count", nullable = false)
	@Builder.Default
	private int nutrientCount = 0;

	@Column(name = "points", nullable = false)
	@Builder.Default
	private int points = 0;

	public void changePassword(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public static Member createLocalMember(
		String phoneNumber,
		String name,
		LocalDate birthDate,
		MemberRole role,
		String profileImageUrl,
		String passwordHash
	) {
		if (passwordHash == null || passwordHash.isBlank()) {
			throw new BusinessException(SignupErrorCode.PASSWORD_REQUIRED);
		}
		if (phoneNumber == null || phoneNumber.isBlank()) {
			throw new BusinessException(SignupErrorCode.OTP_PHONE_NUMBER_REQUIRED);
		}
		if (name == null || name.isBlank()) {
			throw new BusinessException(SignupErrorCode.INVALID_PROFILE_NAME);
		}
		if (birthDate == null) {
			throw new BusinessException(SignupErrorCode.INVALID_PROFILE_BIRTHDATE);
		}
		Member m = new Member();
		m.phoneNumber = phoneNumber;
		m.name = name;
		m.birthDate = birthDate;
		m.role = role;
		m.profileImageUrl = profileImageUrl;
		m.status = MemberStatus.ACTIVE;
		m.passwordHash = passwordHash;
		return m;
	}

	public void block() {
		this.status = MemberStatus.BLOCKED;
	}

	public void activate() {
		this.status = MemberStatus.ACTIVE;
	}
    public void updateToDefaultProfileImage(String defaultProfileImageUrl) {
        this.profileImageUrl = defaultProfileImageUrl;
    }

    public void updateProfile(String profileImageUrl, String profileName) {
        this.profileImageUrl = profileImageUrl;
        this.profileName = profileName;
    }

	public void addPoints(int points) {
		this.points += points;

		if (this.points >= 10) {
			int nutrientsToAdd = this.points / 10;
			this.nutrientCount += nutrientsToAdd;
			this.points = this.points % 10;
		}
	}
}
