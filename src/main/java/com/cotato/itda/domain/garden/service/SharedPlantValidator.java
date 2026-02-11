package com.cotato.itda.domain.garden.service;

import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantLog;
import com.cotato.itda.domain.garden.enums.GardenTimeRule;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.cotato.itda.domain.garden.exception.SharedPlantException;
import com.cotato.itda.domain.garden.exception.code.SharedPlantErrorCode;
import com.cotato.itda.domain.garden.repository.SharedPlantLogRepository;
import com.cotato.itda.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class SharedPlantValidator {

    private final SharedPlantLogRepository sharedPlantLogRepository;

    // 참여자 검증
    public void validateParticipant(SharedPlant sharedPlant, Long memberId) {
        if (!(memberId.equals(sharedPlant.getMemberA().getId()) || memberId.equals(sharedPlant.getMemberB().getId()))) {
            throw new SharedPlantException(SharedPlantErrorCode.NOT_A_PARTICIPANT);
        }
    }

    // 완료되지 않은 상태 검증
    public void validateNotCompleted(SharedPlant sharedPlant) {
        if (sharedPlant.getStatus() == SharedPlantStatus.COMPLETED) {
            throw new SharedPlantException(SharedPlantErrorCode.CANNOT_ACTION_COMPLETED_PLANT);
        }
    }

    // 심어진 상태 검증
    public void validatePlanted(SharedPlant sharedPlant) {
        if (!sharedPlant.isPlanted()) {
            throw new SharedPlantException(SharedPlantErrorCode.NOT_PLANTED);
        }
    }

    // 심어지지 않은 상태 검증
    public void validateNotPlanted(SharedPlant sharedPlant) {
        if (sharedPlant.isPlanted()) {
            throw new SharedPlantException(SharedPlantErrorCode.ALREADY_PLANTED);
        }
    }

    // 물주기 가능 여부 검증 (72시간 시듦 체크 + 연속 물 주기 체크)
    public void validateCanWater(SharedPlant sharedPlant, Long memberId) {
        validatePlanted(sharedPlant);
        validateNotCompleted(sharedPlant);

        // 72시간 + 시듦 상태면 영양제 먼저 줘야함
        boolean wasWithered = sharedPlant.getStatus() == SharedPlantStatus.WITHERED;
        if (wasWithered && sharedPlant.getLastWateredAt() != null
                && ChronoUnit.HOURS.between(sharedPlant.getLastWateredAt(), LocalDateTime.now()) >= GardenTimeRule.NUTRITION_AVAILABLE.getHours()) {
            SharedPlantLog lastLog = sharedPlantLogRepository.findTopBySharedPlantOrderByCreatedAtDesc(sharedPlant).orElse(null);

            boolean isAfterMyNutrition = lastLog != null
                    && lastLog.isUsedNutrient()
                    && lastLog.getWateredBy().equals(memberId);

            if (!isAfterMyNutrition) {
                throw new SharedPlantException(SharedPlantErrorCode.WITHERED_REQUIRES_NUTRIENT);
            }
        }

        if (sharedPlant.getLastWateredBy() != null && memberId.equals(sharedPlant.getLastWateredBy())) {
            throw new SharedPlantException(SharedPlantErrorCode.CANNOT_WATER_CONSECUTIVELY);
        }
    }

    // 영양제 주기 가능 여부 검증 (WITHERED + 72h + 영양제 보유)
    public void validateCanGiveNutrient(SharedPlant sharedPlant, Member member) {
        validatePlanted(sharedPlant);
        validateNotCompleted(sharedPlant);

        if (sharedPlant.getStatus() != SharedPlantStatus.WITHERED
                || sharedPlant.getLastWateredAt() == null
                || ChronoUnit.HOURS.between(sharedPlant.getLastWateredAt(), LocalDateTime.now()) < GardenTimeRule.NUTRITION_AVAILABLE.getHours()
        ) {
            throw new SharedPlantException(SharedPlantErrorCode.CANNOT_GIVE_NUTRIENT);
        }

        if (member.getNutrientCount() <= 0) {
            throw new SharedPlantException(SharedPlantErrorCode.DONT_HAVE_NUTRIENT);
        }
    }
}
