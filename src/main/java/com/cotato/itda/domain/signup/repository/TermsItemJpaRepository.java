package com.cotato.itda.domain.signup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cotato.itda.domain.signup.entity.TermsItemEntity;

public interface TermsItemJpaRepository extends JpaRepository<TermsItemEntity, Long> {

	/**
	 * 특정 번들(FK) + ACTIVE 약관 아이템들을 displayOrder 오름차순으로 모두 조회
	 * Bundle_Id: TermsItemEntity 안에 있는 bundle.id가
	 * AndStatus: TermsItemEntity 안에 있는 status가
	 * OrderByDisplayOrderAsc: TermsItemEntity 안에 있는 displayOrder를 오름차순으로 정렬
	 * select * FROM terms_items ti WHERE ti.bundle_id=1 and ti.status='ACTIVE' order by ti.display_order asc;
	 */
	List<TermsItemEntity> findByBundle_IdAndStatusOrderByDisplayOrderAsc(
		Long bundleId,
		String status
	);

	@Query("""
		select i
		from TermsItemEntity i
		join i.bundle b
		where b.bundleType = :bundleType
		and b.bundleVersion = :bundleVersion
		and b.status = 'ACTIVE'
		and i.status = 'ACTIVE'
		order by i.displayOrder asc		
		""")
	List<TermsItemEntity> findActiveByBundle(
		@Param("bundleType") String bundleType,
		@Param("bundleVersion") String bundleVersion
	);
}
