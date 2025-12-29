package com.cotato.itda.domain.member.repository;

import com.cotato.itda.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
