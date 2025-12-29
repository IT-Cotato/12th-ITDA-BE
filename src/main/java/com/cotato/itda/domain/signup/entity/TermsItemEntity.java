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

	@Column(name = "code", nullable = false, length = 50)
	private String code;

	@Column(name = "version", nullable = false, length = 20)
	private String version;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Column(name = "required", nullable = false)
	private boolean required;

	@Column(name = "display_order", nullable = false)
	private int displayOrder;

	@Column(name = "detail_url", length = 500)
	private String detailUrl;

	// ACTIVE / INACTIVE
	@Column(name = "status", nullable = false, length = 20)
	private String status;

}