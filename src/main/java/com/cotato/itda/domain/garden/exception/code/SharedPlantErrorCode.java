package com.cotato.itda.domain.garden.exception.code;

import com.cotato.itda.global.error.constant.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SharedPlantErrorCode implements ErrorCode {

    ALREADY_HAS_SHARED_PLANT(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_ALREADY_HAS_SHARED_PLANT",
            "이미 키우고 있는 식물이 있습니다."),

    CANNOT_WATER_CONSECUTIVELY(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_CANNOT_WATER_CONSECUTIVELY",
            "연속으로 물을 줄 수 없습니다."
    ),

    DONT_HAVE_NUTRIENT(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_DONT_HAVE_NUTRIENT",
            "영양제가 없습니다."
    ),

    SHARED_PLANT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "SHARED_PLANT_404_SHARED_PLANT_NOT_FOUND",
            "함께 키우는 식물을 찾지 못했습니다."
    ),

    NOT_A_PARTICIPANT(
            HttpStatus.FORBIDDEN,
            "SHARED_PLANT_403_NOT_A_PARTICIPANT",
            "함께 키우는 식물 참여자가 아닙니다."
    ),

    CANNOT_ACTION_COMPLETED_PLANT(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_CANNOT_ACTION_COMPLETED_PLANT",
            "키우기가 완료된 식물에는 행동할 수 없습니다."
    ),

    WITHERED_REQUIRES_NUTRIENT(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_WITHERED_REQUIRES_NUTRIENT",
            "시든 식물에는 영양제가 필요합니다."
    ),

    ALREADY_PLANTED(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_ALREADY_PLANTED",
            "이미 씨앗이 심겨진 식물입니다."
    ),

    NOT_PLANTED(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_NOT_PLANTED",
            "아직 씨앗이 심기지 않았습니다."
    ),

    CANNOT_GIVE_NUTRIENT(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_CANNOT_GIVE_NUTRIENT",
            "아직 영양제를 줄 수 없습니다."
    ),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
