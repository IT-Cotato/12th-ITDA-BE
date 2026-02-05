package com.cotato.itda.domain.websocket.dto;

public enum ChatEventType {
	ROOM_CREATED, // 임시 화면에서 첫 SEND 후 roomId를 알려주기 위한 이벤트
	ROOM_LIST_UPDATED, // 채팅 목록 갱신
	READ_STATE_UPDATED // 읽음 갱신 이벤트
}
