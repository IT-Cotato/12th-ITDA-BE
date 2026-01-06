package com.cotato.itda.domain.signup.repository;

import java.util.List;

import com.cotato.itda.domain.signup.dto.TermsItem;
import com.cotato.itda.domain.signup.model.TermsBundle;

public interface TermsPolicyRepository {
	TermsBundle getSignupTermsBundle();

	List<TermsItem> getSignupTerms(Long bundleId);

	List<TermsPolicyItem> getTermsByBundle(String bundleType, String bundleVersion);

	record TermsPolicyItem(
		Long id,
		String code,
		String version,
		boolean required,
		int displayOrder
	) {
	}
}
