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
public enum ChatGoal {

    DAY_1(1),
    DAY_2(2),
    DAY_3(3),
    DAY_4(4),
    DAY_5(5),
    DAY_6(6),
    EVERY_DAY(7),
    NONE(0),
    ;

    private final Integer value;

    @JsonValue
    public Integer getDescription() {
        return this.value;
    }

    @JsonCreator
    public static ChatGoal from(Integer value) {
        for (ChatGoal goal : ChatGoal.values()) {
            if (goal.value.equals(value)) {
                return goal;
            }
        }

        throw new FriendshipException(FriendshipErrorCode.INVALID_CHAT_GOAL, Map.of("chatGoal", String.valueOf(value)));
    }
}
