package com.cotato.itda.global.common.constant;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * S3 버킷 내 prefix 구조 정의
 * 도메인별로 이미지를 분류하여 저장
 */
public enum S3Folder {

    // 회원 프로필 이미지 저장 (profile/)
    PROFILE("profile");

    private final String value;

    S3Folder(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

}
