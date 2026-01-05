package com.cotato.itda.domain.signup.entity;

import com.cotato.itda.global.entity.BaseEntity;
import com.cotato.itda.global.entity.BaseTimeEntity;

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
	name = "member_terms_consents",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_member_terms_item", columnNames = {"member_id", "terms_item_id"})
	},
	indexes = {
		@Index(name = "idx_mtc_member", columnList = "member_id"),
		@Index(name = "idx_mtc_terms_item", columnList = "terms_item_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTermsConsentEntity extends BaseEntity {

	// 여기서는 aggregate 분리를 위해 엔티티 연관 대신 ID만 들고 가도 됨.
	// 하지만 FK 무결성/조인 편의 때문에 ManyToOne도 실무에서 많이 씀.
	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "terms_item_id", nullable = false)
	private Long termsItemId;

	@Column(name = "agreed", nullable = false)
	private boolean agreed;

	public static MemberTermsConsentEntity create(Long memberId, Long termsItemId, boolean agreed) {
		MemberTermsConsentEntity e = new MemberTermsConsentEntity();
		e.memberId = memberId;
		e.termsItemId = termsItemId;
		e.agreed = agreed;
		return e;
	}

	public void changeAgreed(boolean agreed) {
		this.agreed = agreed;
	}
}