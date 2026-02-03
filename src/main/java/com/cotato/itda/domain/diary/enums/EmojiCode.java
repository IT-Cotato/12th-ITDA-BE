package com.cotato.itda.domain.diary.enums;

import com.cotato.itda.global.error.constant.DiaryErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum EmojiCode {

    // 기쁨 (HAPPY)
    HAPPY_GLAD("기쁜"),
    HAPPY_HAPPY("행복한"),
    HAPPY_JOY("즐거운"),
    HAPPY_COOL("멋진"),
    HAPPY_PROUD("흐뭇한"),
    HAPPY_GRATEFUL("감사한"),
    HAPPY_EXCITED("정말 신나는"),
    HAPPY_SPECIAL("특별한"),
    HAPPY_LOVELY("사랑스러운"),
    HAPPY_BEST("최고의"),
    HAPPY_SATISFIED("만족스러운"),
    HAPPY_GOOD("좋은"),

    // 슬픔 (SAD)
    SAD_SAD("슬픈"),
    SAD_DEPRESSED("우울한"),
    SAD_WORRIED("걱정스러운"),
    SAD_DISAPPOINTED("실망스러운"),
    SAD_ABSURD("황당한"),
    SAD_BAD("별로인"),
    SAD_HARD("힘든"),
    SAD_CRY("울고 싶은"),
    SAD_SHOCKED("놀란"),
    SAD_EMBARRASSED("당황스러운"),
    SAD_SCARED("두려운"),
    SAD_UPSET("속상한"),

    // 화남 (ANGRY)
    ANGRY_ANGRY("화난"),
    ANGRY_ANNOYED("짜증나는"),
    ANGRY_FRUSTRATED("답답한"),
    ANGRY_UNEASY("심란한"),
    ANGRY_SUSPICIOUS("의심스러운"),
    ANGRY_TERRIBLE("끔찍한"),
    ANGRY_AWKWARD("어색한"),
    ANGRY_FURIOUS("분한"),
    ANGRY_RESENTFUL("원망스러운"),
    ANGRY_HATE("미운"),

    // 일상 (DAILY)
    DAILY_PLAIN("무난한"),
    DAILY_SECRET("비밀스러운"),
    DAILY_SILENT("말하고 싶지 않은"),
    DAILY_SICK("아픈"),
    DAILY_SOSO("그저 그런"),
    DAILY_UNSURE("잘 모르겠는"),
    DAILY_SHY("숨고 싶은"),
    DAILY_CONFUSED("혼란스러운"),
    DAILY_TIRED("피곤한"),
    DAILY_PLAYFUL("장난치고 싶은"),
    DAILY_BORED("심심한"),
    DAILY_AMAZING("신기한");

    private final String description; // 수식형

    @JsonCreator
    public static EmojiCode from(String value) {
        if (value == null) {
            return null;
        }
        try {
            return EmojiCode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(DiaryErrorCode.INVALID_EMOJI_CODE, Map.of("emojiCode", value));
        }
    }

}