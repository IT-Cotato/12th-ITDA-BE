package com.cotato.itda.domain.member.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "member.withdrawal")
public class MemberWithdrawalProperties {
	private int gracePeriodDays = 7;
	private String cleanupCron = "0 0 3 * * *";
	private String anonymizedPhonePrefix = "WD";
	private String anonymizedName = "탈퇴한 회원";
	private String anonymizedPasswordHash = "WITHDRAWN";
}
