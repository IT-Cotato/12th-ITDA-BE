package com.cotato.itda.global.security.jwt.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;

public enum JwtMacAlgorithm {
	HS256, HS384, HS512;

	public MacAlgorithm toJjwt() {
		return switch (this) {
			case HS256 -> Jwts.SIG.HS256;
			case HS384 -> Jwts.SIG.HS384;
			case HS512 -> Jwts.SIG.HS512;
		};
	}

}
