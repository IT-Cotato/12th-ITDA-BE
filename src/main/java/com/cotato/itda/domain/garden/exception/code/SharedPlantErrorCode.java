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

    ALREADY_WATERED(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_ALREADY_WATERED",
            "이미 물을 줬습니다."
    ),

    EXCEED_WATER_COUNT(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_EXCEED_WATER_COUNT",
            "물 주기 가능 횟수를 초과했습니다."
    ),

    NO_PERMISSION_TO_WATER_SOLO_PLANT(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_NO_PERMISSION_TO_WATER_SOLO_PLANT",
            "혼자 돌봄 모드에서 물을 줄 권한이 없습니다."
    ),
    CANNOT_WATER_CONSECUTIVELY(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_CANNOT_WATER_CONSECUTIVELY",
            "연속으로 물을 줄 수 없습니다."
    ),

    NOT_FOUND_STRATEGY(
            HttpStatus.NOT_FOUND,
            "SHARED_PLANT_404_NOT_FOUND_STRATEGY",
            "적절한 기본 물주기 전략을 찾을 수 없습니다."
    ),

    DONT_HAVE_NUTRIENT(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_DONT_HAVE_NUTRIENT",
            "영양제가 없습니다."
    ),

    ALREADY_GAVE_NUTRIENT_TODAY(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_ALREADY_GAVE_NUTRIENT_TODAY",
            "오늘 이미 영양제를 줬습니다."
    ),

    SHARED_PLANT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "SHARED_PLANT_400_SHARED_PLANT_NOT_FOUND",
            "함께 키우는 식물을 찾지 못했습니다."
    ),

    CANNOT_USE_NUTRIENT_YET(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_CANNOT_USE_NUTRIENT_YET",
            "아직 영양제를 줄 수 없습니다."
    ),

    NOT_A_PARTICIPANT(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_NOT_A_PARTICIPANT",
            "함께 키우는 식물 참여자가 아닙니다."
    ),

    CANNOT_WATER_COMPLETED_PLANT(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_CANNOT_WATER_COMPLETED_PLANT",
            "키우기가 완료된 식물에는 물을 줄 수 없습니다."
    ),

    WITHERED_REQUIRES_NUTRIENT(
            HttpStatus.BAD_REQUEST,
            "SHARED_PLANT_400_WITHERED_REQUIRES_NUTRIENT",
            "시든 식물에는 영양제가 필요합니다."
    ),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
