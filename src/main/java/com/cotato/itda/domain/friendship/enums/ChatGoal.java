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

    DAY_1("주 1일"),
    DAY_2("주 2일"),
    DAY_3("주 3일"),
    DAY_4("주 4일"),
    DAY_5("주 5일"),
    DAY_6("주 6일"),
    EVERY_DAY("매일"),
    NONE("선택하지 않음"),
    ;

    private final String description;

    @JsonValue
    public String getDescription() {
        return this.description;
    }

    @JsonCreator
    public static ChatGoal from(String value) {
        for (ChatGoal goal : ChatGoal.values()) {
            if (goal.description.equals(value)) {
                return goal;
            }
        }

        throw new FriendshipException(FriendshipErrorCode.INVALID_CHAT_GOAL, Map.of("chatGoal", value));
    }
}
