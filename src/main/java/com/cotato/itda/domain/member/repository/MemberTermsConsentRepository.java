package com.cotato.itda.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cotato.itda.domain.signup.entity.MemberTermsConsentEntity;

public interface MemberTermsConsentRepository extends JpaRepository<MemberTermsConsentEntity, Long> {

}
