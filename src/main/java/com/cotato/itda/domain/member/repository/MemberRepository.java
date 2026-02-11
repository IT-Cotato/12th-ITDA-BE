package com.cotato.itda.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.cotato.itda.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByPhoneNumber(String phoneNumber);

	Boolean existsByPhoneNumber(String phoneNumber);

	// 핸드폰 번호와 이름으로 조회
	Optional<Member> findByPhoneNumberAndName(String phoneNumber, String name);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
        update Member m
           set m.passwordHash = :encodedPassword
         where m.phoneNumber = :phoneNumber
    """)
	void updatePasswordByPhoneNumber(String encodedPassword, String phoneNumber);

}
