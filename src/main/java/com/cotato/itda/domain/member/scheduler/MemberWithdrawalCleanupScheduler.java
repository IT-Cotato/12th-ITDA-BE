package com.cotato.itda.domain.member.scheduler;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.member.config.MemberWithdrawalProperties;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.entity.MemberStatus;
import com.cotato.itda.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberWithdrawalCleanupScheduler {

	private final MemberRepository memberRepository;
	private final MemberWithdrawalProperties memberWithdrawalProperties;

	@Scheduled(cron = "${member.withdrawal.cleanup-cron}")
	@Transactional
	public void finalizeExpiredWithdrawals() {
		OffsetDateTime cutoff = OffsetDateTime.now()
			.minusDays(memberWithdrawalProperties.getGracePeriodDays());
		List<Member> members = memberRepository.findAllByStatusAndWithdrawnAtBefore(
			MemberStatus.WITHDRAWAL_PENDING,
			cutoff
		);

		for (Member member : members) {
			member.finalizeWithdrawal(
				buildAnonymizedPhoneNumber(member),
				memberWithdrawalProperties.getAnonymizedName(),
				memberWithdrawalProperties.getAnonymizedPasswordHash()
			);
		}

		if (!members.isEmpty()) {
			log.info("탈퇴 유예 기간 만료 회원 최종 처리 완료: count={}", members.size());
		}
	}

	private String buildAnonymizedPhoneNumber(Member member) {
		String memberId = String.valueOf(member.getId());
		String prefix = memberWithdrawalProperties.getAnonymizedPhonePrefix();
		int prefixLength = Math.max(0, Member.PHONE_NUMBER_MAX_LENGTH - memberId.length());
		String safePrefix = prefix.substring(0, Math.min(prefix.length(), prefixLength));
		return safePrefix + memberId;
	}
}
