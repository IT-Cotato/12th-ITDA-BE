package com.cotato.itda.domain.garden.exception.code;

import com.cotato.itda.global.error.constant.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SharedPlantInviteErrorCode implements ErrorCode {

        ALREADY_HAS_PENDING_INVITE(
                        HttpStatus.BAD_REQUEST,
                        "SHARED_PLANT_INVITE_400_ALREADY_HAS_PENDING_INVITE",
                        "이미 초대 요청이 있는 친구입니다."
        ),
        SELF_INVITE_NOT_ALLOWED(
                        HttpStatus.BAD_REQUEST,
                        "SHARED_PLANT_INVITE_400_SELF_INVITE_NOT_ALLOWED",
                        "본인 자신을 초대할 수 없습니다."
        ),
        INVITE_NOT_FOUND(
                        HttpStatus.NOT_FOUND,
                        "SHARED_PLANT_INVITE_404_NOT_FOUND",
                        "존재하지 않는 초대입니다."
        ),
        NOT_INVITEE(
                        HttpStatus.FORBIDDEN,
                        "SHARED_PLANT_INVITE_403_NOT_INVITEE",
                        "초대받은 사람만 접근할 수 있습니다."
        ),
        INVITE_NOT_PENDING(
                        HttpStatus.BAD_REQUEST,
                        "SHARED_PLANT_INVITE_400_NOT_PENDING",
                        "대기 중인 초대장만 처리할 수 있습니다."
        ),
        INVALID_INVITE_STATUS(
                        HttpStatus.BAD_REQUEST,
                        "SHARED_PLANT_INVITE_400_INVALID_INVITE_STATUS",
                        "초대장 상태는 ACCEPTED 또는 REJECTED여야 합니다."
        );

        private final HttpStatus httpStatus;
        private final String code;
        private final String message;
}
