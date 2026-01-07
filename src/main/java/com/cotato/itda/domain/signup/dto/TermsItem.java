package com.cotato.itda.domain.signup.dto;

public record TermsItem(
	String code,
	String version,
	String title,
	boolean required,
	int displayOrder,
	String detailUrl
){}