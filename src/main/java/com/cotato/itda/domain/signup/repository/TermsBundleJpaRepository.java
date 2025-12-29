package com.cotato.itda.domain.signup.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cotato.itda.domain.signup.entity.TermsBundleEntity;

public interface TermsBundleJpaRepository extends JpaRepository<TermsBundleEntity,Long> {

	Optional<TermsBundleEntity> findTopByBundleTypeAndStatusOrderByPublishedAtDesc(
		String bundleType,
		String status
	);
}
