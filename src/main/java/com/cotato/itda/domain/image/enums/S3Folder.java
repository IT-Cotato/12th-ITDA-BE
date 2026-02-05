package com.cotato.itda.domain.image.enums;

/**
 * S3 버킷 내 prefix 구조 정의
 * 도메인별로 이미지를 분류하여 저장
 */
public enum S3Folder {

    // 회원 프로필 이미지 저장 (profile/)
    PROFILE("profile"),

    // 공유일기 이미지 저장 (diary/)
    DIARY("diary"),

    // 챌린지 이미지 저장 (challenge/)
    CHALLENGE("challenge"),

    // 채팅 이미지 저장 (chat/)
    CHAT("chat");

    private final String value;

    S3Folder(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
