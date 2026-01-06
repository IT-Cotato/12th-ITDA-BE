package com.cotato.itda.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cotato.itda.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByPhoneNumber(String phoneNumber);

	Boolean existsByPhoneNumber(String phoneNumber);
}
