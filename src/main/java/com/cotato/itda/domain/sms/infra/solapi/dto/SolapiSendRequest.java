package com.cotato.itda.domain.sms.infra.solapi.dto;

import java.util.List;

public record SolapiSendRequest(
	List<Message> messages
) {
	public record Message(
		String from,
		String to,
		String text
	) {
	}
}