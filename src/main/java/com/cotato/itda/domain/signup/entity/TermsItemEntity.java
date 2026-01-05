package com.cotato.itda.domain.signup.entity;

import com.cotato.itda.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "terms_items",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_bundle_item", columnNames = {"bundle_id", "code", "version"})
	},
	indexes = {
		@Index(name = "idx_items_lookup", columnList = "bundle_id,status,display_order")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsItemEntity extends BaseEntity {

	// 어떤 번들에 속하는지 (FK)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "bundle_id", nullable = false)
	private TermsBundleEntity bundle;

	// 약관 코드
	// 예: TERMS_OF_SERVICE, PRIVACY_POLICY
	@Column(name = "code", nullable = false, length = 50)
	private String code;

	// 약관 버전
	// 예: 2025-12
	@Column(name = "version", nullable = false, length = 20)
	private String version;

	// 약관 제목
	// 예: "서비스 이용 약관", "개인정보 처리 방침"
	@Column(name = "title", nullable = false, length = 200)
	private String title;

	// 필수 동의 여부
	// true = 필수, false = 선택
	@Column(name = "required", nullable = false)
	private boolean required;

	// 화면 표시 순서
	// 낮은 숫자가 먼저 표시됨
	@Column(name = "display_order", nullable = false)
	private int displayOrder;

	// 상세 약관 URL
	// 예: "https://example.com/terms-of-service-2025-12"
	@Column(name = "detail_url", length = 500)
	private String detailUrl;

	// 약관 현재 상태
	// ACTIVE / INACTIVE
	@Column(name = "status", nullable = false, length = 20)
	private String status;

}