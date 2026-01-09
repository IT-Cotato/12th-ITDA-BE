package com.cotato.itda.domain.friendship.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FriendshipStatus {

    PENDING("친구 설정 이전"),
    ACTIVE("친구 설정 완료"),
    ;
    private final String description;
}
