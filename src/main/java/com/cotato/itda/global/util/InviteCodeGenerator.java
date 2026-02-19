package com.cotato.itda.global.util;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class InviteCodeGenerator {
	private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int LEN = 6;

	private final SecureRandom random = new SecureRandom();

	public String generate() {
		StringBuilder sb = new StringBuilder(LEN);
		for (int i = 0; i < LEN; i++) {
			int idx = random.nextInt(CHARSET.length());
			sb.append(CHARSET.charAt(idx));
		}
		return sb.toString();
	}
}
