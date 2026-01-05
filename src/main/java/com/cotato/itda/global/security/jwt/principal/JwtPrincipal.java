package com.cotato.itda.global.security.jwt.principal;

public record JwtPrincipal(
	Long memberId,
	String subject,
	String purpose

) {
}
