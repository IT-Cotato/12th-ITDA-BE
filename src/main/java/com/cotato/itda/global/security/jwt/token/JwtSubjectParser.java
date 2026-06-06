package com.cotato.itda.global.security.jwt.token;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.cotato.itda.global.error.constant.JwtErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import io.jsonwebtoken.Claims;

@Component
public class JwtSubjectParser {

	public Long parseMemberId(Claims claims) {
		Objects.requireNonNull(claims, "claims must not be null");

		String subject = claims.getSubject();
		if (subject == null || subject.isBlank()) {
			throw new BusinessException(JwtErrorCode.INVALID_TOKEN);
		}

		try {
			return Long.parseLong(subject);
		} catch (NumberFormatException e) {
			throw new BusinessException(JwtErrorCode.INVALID_TOKEN);
		}
	}
}
