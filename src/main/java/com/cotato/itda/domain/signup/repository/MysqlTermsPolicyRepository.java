package com.cotato.itda.domain.signup.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.cotato.itda.domain.signup.dto.TermsItem;
import com.cotato.itda.domain.signup.entity.TermsBundleEntity;
import com.cotato.itda.domain.signup.entity.TermsItemEntity;
import com.cotato.itda.domain.signup.model.TermsBundle;
import com.cotato.itda.global.error.constant.SignupErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MysqlTermsPolicyRepository implements TermsPolicyRepository {

	private static final String SIGNUP_BUNDLE_TYPE = "SIGNUP";
	private static final String ACTIVE_STATUS = "ACTIVE";

	private final TermsBundleJpaRepository termsBundleJpaRepository;
	private final TermsItemJpaRepository termsItemJpaRepository;

	@Override
	public TermsBundle getSignupTermsBundle() {
		TermsBundleEntity bundle = loadCurrentSignupBundle();

		return new TermsBundle(bundle.getId(), bundle.getBundleType(), bundle.getBundleVersion());
	}

	@Override
	public List<TermsItem> getSignupTerms(Long bundleId) {
		List<TermsItemEntity> termsItems = termsItemJpaRepository
			.findByBundle_IdAndStatusOrderByDisplayOrderAsc(bundleId, ACTIVE_STATUS);

		return termsItems.stream()
			.map(item -> new TermsItem(
				item.getCode(),
				item.getVersion(),
				item.getTitle(),
				item.isRequired(),
				item.getDisplayOrder(),
				item.getDetailUrl()
			))
			.toList();
	}

	@Override
	public List<TermsPolicyItem> getTermsByBundle(String bundleType, String bundleVersion) {
		if (bundleType == null || bundleType.isBlank()) {
			throw new BusinessException(SignupErrorCode.INVALID_TERMS_BUNDLE_PARAMETERS);
		}
		if (bundleVersion == null || bundleVersion.isBlank()) {
			throw new BusinessException(SignupErrorCode.INVALID_TERMS_BUNDLE_PARAMETERS);
		}
		List<TermsItemEntity> termsItems = termsItemJpaRepository
			.findActiveByBundle(bundleType, bundleVersion);
		return termsItems.stream()
			.map(item -> new TermsPolicyItem(
				item.getId(),
				item.getCode(),
				item.getVersion(),
				item.isRequired(),
				item.getDisplayOrder()
			))
			.toList();
	}

	private TermsBundleEntity loadCurrentSignupBundle() {
		return termsBundleJpaRepository
			.findTopByBundleTypeAndStatusOrderByPublishedAtDesc(SIGNUP_BUNDLE_TYPE, ACTIVE_STATUS)
			.orElseThrow(() -> new BusinessException(SignupErrorCode.TERMS_BUNDLE_NOT_FOUND));
	}
}
