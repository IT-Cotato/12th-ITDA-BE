package com.cotato.itda.global.security.jwt.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtPurpose {
	ACCESS,
	REFRESH,
}
