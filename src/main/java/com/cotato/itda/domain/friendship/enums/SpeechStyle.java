package com.cotato.itda.domain.friendship.enums;

import com.cotato.itda.domain.friendship.exception.FriendshipException;
import com.cotato.itda.domain.friendship.exception.code.FriendshipErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SpeechStyle {

    FORMAL("존댓말"),
    INFORMAL("반말"),
    ;

    private final String korean;

    @JsonCreator
    public static SpeechStyle from(String value) {
        for (SpeechStyle style : SpeechStyle.values()) {
            if (style.korean.equals(value)) {
                return style;
            }
        }

        throw new FriendshipException(FriendshipErrorCode.INVALID_SPEECH_STYLE, Map.of("speechStyle", value));
    }

    @JsonValue
    public String toKorean() {
        return this.korean;
    }
}
