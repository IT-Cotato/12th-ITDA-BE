package com.cotato.itda.domain.garden.service.command;

import com.cotato.itda.domain.garden.converter.SharedPlantConverter;
import com.cotato.itda.domain.garden.converter.SharedPlantLogConverter;
import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.enums.GrowthType;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.cotato.itda.domain.garden.exception.SharedPlantException;
import com.cotato.itda.domain.garden.exception.code.SharedPlantErrorCode;
import com.cotato.itda.domain.garden.repository.SharedPlantLogRepository;
import com.cotato.itda.domain.garden.repository.SharedPlantRepository;
import com.cotato.itda.domain.garden.service.GardenStateCalculator;
import com.cotato.itda.domain.garden.service.SharedPlantValidator;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedPlantCommandServiceImpl implements SharedPlantCommandService {

    private final SharedPlantRepository sharedPlantRepository;
    private final MemberRepository memberRepository;
    private final SharedPlantLogRepository sharedPlantLogRepository;
    private final GardenStateCalculator gardenStateCalculator;
    private final SharedPlantValidator sharedPlantValidator;

    @Override
    public SharedPlantResDTO.PlantActionResDTO water(Long sharedPlantId, Long memberId) {
        Member member = findMemberById(memberId);
        SharedPlant sharedPlant = findSharedPlantById(sharedPlantId);

        // 참여자 검증
        sharedPlantValidator.validateParticipant(sharedPlant, memberId);

        // 물주기 가능 여부 검증
        sharedPlantValidator.validateCanWater(sharedPlant, memberId);

        // 혼자 돌봄 모드 종료 로직
        if (sharedPlant.getIsSoloMode() && !memberId.equals(sharedPlant.getSoloPowerMemberId())) {
            sharedPlant.exitSoloMode();
        }

        boolean stageChanged = sharedPlant.water(GrowthType.WATER.getGrowth(), member);

        // 성장 완료 시 COMPLETED로
        if (sharedPlant.hasReachedMaxGrowth()) {
            sharedPlant.complete();
        }

        // 로그 저장
        sharedPlantLogRepository.save(SharedPlantLogConverter.toSharedPlantLog(sharedPlant, memberId, true, GrowthType.WATER.getGrowth(), false, stageChanged));

        return SharedPlantConverter.toPlantActionResDTO(
                sharedPlant, member,
                gardenStateCalculator.calculateState(sharedPlant),
                gardenStateCalculator.calculatePercentage(sharedPlant)
        );
    }

    @Override
    public SharedPlantResDTO.PlantActionResDTO giveNutrient(Long sharedPlantId, Long memberId) {
        Member member = findMemberById(memberId);
        SharedPlant sharedPlant = findSharedPlantById(sharedPlantId);

        // 참여자 검증
        sharedPlantValidator.validateParticipant(sharedPlant, memberId);

        // 영양제 주기 가능 여부 검증
        sharedPlantValidator.validateCanGiveNutrient(sharedPlant, member);

        // 혼자 돌봄 모드 -> 공동 돌봄 모드로
        if (sharedPlant.getIsSoloMode() && !memberId.equals(sharedPlant.getSoloPowerMemberId())) {
            sharedPlant.exitSoloMode();
        }

        boolean stageChanged = sharedPlant.nutrient(GrowthType.NUTRIENT.getGrowth(), member);

        // 로그 저장
        sharedPlantLogRepository.save(SharedPlantLogConverter.toSharedPlantLog(sharedPlant, memberId, true, GrowthType.NUTRIENT.getGrowth(), true, stageChanged));

        return SharedPlantConverter.toPlantActionResDTO(
                sharedPlant, member,
                gardenStateCalculator.calculateState(sharedPlant),
                gardenStateCalculator.calculatePercentage(sharedPlant)
        );
    }

    @Override
    public SharedPlantResDTO.PlantActionResDTO plantSeed(Long sharedPlantId, Long memberId) {
        Member member = findMemberById(memberId);
        SharedPlant sharedPlant = findSharedPlantById(sharedPlantId);

        // 참여자 검증
        sharedPlantValidator.validateParticipant(sharedPlant, memberId);

        // 심어지지 않은 상태 검증
        sharedPlantValidator.validateNotPlanted(sharedPlant);

        sharedPlant.plant();

        return SharedPlantConverter.toPlantActionResDTO(
                sharedPlant, member,
                gardenStateCalculator.calculateState(sharedPlant),
                gardenStateCalculator.calculatePercentage(sharedPlant)
        );
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private SharedPlant findSharedPlantById(Long sharedPlantId) {
        return sharedPlantRepository.findById(sharedPlantId)
                .orElseThrow(() -> new SharedPlantException(SharedPlantErrorCode.SHARED_PLANT_NOT_FOUND));
    }
}
