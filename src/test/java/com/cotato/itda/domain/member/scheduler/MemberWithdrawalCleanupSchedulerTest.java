package com.cotato.itda.domain.member.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cotato.itda.domain.member.config.MemberWithdrawalProperties;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.entity.MemberStatus;
import com.cotato.itda.domain.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberWithdrawalCleanupSchedulerTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private MemberWithdrawalProperties memberWithdrawalProperties;

	@InjectMocks
	private MemberWithdrawalCleanupScheduler scheduler;

	@Test
	void finalizeExpiredWithdrawals_anonymizes_members_and_marks_withdrawn() {
		Member member = Member.builder()
			.id(1L)
			.phoneNumber("01012345678")
			.name("홍길동")
			.status(MemberStatus.WITHDRAWAL_PENDING)
			.passwordHash("encoded-password")
			.profileImageUrl("https://example.com/profile.png")
			.inviteCode("ABC123")
			.withdrawnAt(OffsetDateTime.now().minusDays(8))
			.build();
		ArgumentCaptor<OffsetDateTime> cutoffCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

		when(memberWithdrawalProperties.getGracePeriodDays()).thenReturn(7);
		when(memberWithdrawalProperties.getAnonymizedPhonePrefix()).thenReturn("WD");
		when(memberWithdrawalProperties.getAnonymizedName()).thenReturn("탈퇴한 회원");
		when(memberWithdrawalProperties.getAnonymizedPasswordHash()).thenReturn("WITHDRAWN");
		when(memberRepository.findAllByStatusAndWithdrawnAtBefore(
			eq(MemberStatus.WITHDRAWAL_PENDING),
			cutoffCaptor.capture()
		)).thenReturn(List.of(member));

		scheduler.finalizeExpiredWithdrawals();

		assertThat(cutoffCaptor.getValue()).isBeforeOrEqualTo(OffsetDateTime.now().minusDays(7).plusSeconds(1));
		assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
		assertThat(member.getPhoneNumber()).isEqualTo("WD1");
		assertThat(member.getName()).isEqualTo("탈퇴한 회원");
		assertThat(member.getPasswordHash()).isEqualTo("WITHDRAWN");
		assertThat(member.getProfileImageUrl()).isNull();
		assertThat(member.getInviteCode()).isNull();
	}
}
