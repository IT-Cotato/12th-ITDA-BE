package com.cotato.itda.domain.signup.entity;

import java.time.LocalDateTime;

import com.cotato.itda.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Getter
@Entity
@Table(
	name="terms_bundles",
	uniqueConstraints = {
		@UniqueConstraint(name="uk_bundle_id_version", columnNames = {"bundle_type", "bundle_version"})
	},
	indexes = {
		// 현재 적용 중인 약관 묶음을 빠르게 조회하기 위한 인덱스
		// bundleType = SIGNUP, status = ACTIVE, publishedAt 내림차순(최신순)
		// 예: SELECT * FROM terms_bundles WHERE bundle_id = 'SIGNUP' AND status = 'ACTIVE' ORDER BY published_at DESC LIMIT 1;
		@Index(name="idx_bundle_lookup", columnList ="bundle_type, status, published_at")
	}
)
public class TermsBundleEntity extends BaseEntity {

	// 약관 묶음 TYPE
	// 예 : SIGNUP
	@Column(name="bundle_type", nullable=false, length=30)
	private String bundleType;

	// 약관 묶음 버전
	// 예: 2025-12
	@Column(name="bundle_version", nullable = false, length = 20)
	private String bundleVersion;

	// 약관 현재 상태
	// 예: ACTIVE, INACTIVE
	@Column(name="status", nullable = false, length =20)
	private String status;

	// 약관 발행 일시
	// status말고 publishedAt이 필요한 이유
	// - 약관이 언제 발행되었는지 기록하기 위해
	// - 약관의 변경 이력을 관리하거나, 특정 시점에 어떤 약관이 유효했는지 조회할 때 필요
	// - status는 현재 상태만 나타내지만, publishedAt은 시간 정보를 제공
	@Column(name = "published_at", nullable = false)
	private LocalDateTime publishedAt;

}
