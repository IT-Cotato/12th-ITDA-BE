package com.cotato.itda.domain.garden.service.command;

import com.cotato.itda.domain.garden.converter.SharedPlantConverter;
import com.cotato.itda.domain.garden.converter.SharedPlantLogConverter;
import com.cotato.itda.domain.garden.dto.req.SharedPlantReqDTO;
import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantLog;
import com.cotato.itda.domain.garden.enums.GardenTimeRule;
import com.cotato.itda.domain.garden.enums.GrowthType;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.cotato.itda.domain.garden.enums.SupplyType;
import com.cotato.itda.domain.garden.exception.SharedPlantException;
import com.cotato.itda.domain.garden.exception.code.SharedPlantErrorCode;
import com.cotato.itda.domain.garden.repository.SharedPlantLogRepository;
import com.cotato.itda.domain.garden.repository.SharedPlantRepository;
import com.cotato.itda.domain.garden.service.PlantActionResponseBuilder;
import com.cotato.itda.domain.garden.service.command.strategy.WateringStrategy;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedPlantCommandServiceImpl implements SharedPlantCommandService {

    private final List<WateringStrategy> wateringStrategies;
    private final SharedPlantRepository sharedPlantRepository;
    private final MemberRepository memberRepository;
    private final SharedPlantLogRepository sharedPlantLogRepository;
    private final PlantActionResponseBuilder plantActionResponseBuilder;

    @Override
    public SharedPlantResDTO.WaterInfoResDTO waterPlant(Long sharedPlantId, SharedPlantReqDTO.WaterPlantReqDTO dto, Long memberId) {

        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        SharedPlant sharedPlant = sharedPlantRepository.findById(sharedPlantId)
                .orElseThrow(() -> new SharedPlantException(SharedPlantErrorCode.SHARED_PLANT_NOT_FOUND));

        Long memberAId = sharedPlant.getMemberA().getId();
        Long memberBId = sharedPlant.getMemberB().getId();
        Long currentMemberId = currentMember.getId();

        if (!(currentMemberId.equals(memberAId) || currentMemberId.equals(memberBId))) {
            throw new SharedPlantException(SharedPlantErrorCode.NOT_A_PARTICIPANT);
        }

        if (sharedPlant.getStatus() == SharedPlantStatus.COMPLETED) {
            throw new SharedPlantException(SharedPlantErrorCode.CANNOT_WATER_COMPLETED_PLANT);
        }

        boolean wasWithered = sharedPlant.getStatus() == SharedPlantStatus.WITHERED;
        if (wasWithered && dto.supplyType() != SupplyType.NUTRIENT) {
            throw new SharedPlantException(SharedPlantErrorCode.WITHERED_REQUIRES_NUTRIENT);
        }

        if (sharedPlant.getIsSoloMode() && !sharedPlant.getSoloPowerMemberId().equals(currentMemberId)) {
            sharedPlant.exitSoloMode();
        }

        int growthBefore = sharedPlant.getGrowthValue();

        WateringStrategy strategy = wateringStrategies.stream()
                .filter(s -> s.supports(sharedPlant, dto))
                .findFirst()
                .orElseThrow(() -> new SharedPlantException(SharedPlantErrorCode.NOT_FOUND_STRATEGY));

        strategy.water(sharedPlant, currentMember, wateringStrategies);

        if (wasWithered) {
            sharedPlant.revive();
        }

        // 최대 성장 도달 시 COMPLETED 처리
        if (sharedPlant.hasReachedMaxGrowth()) {
            sharedPlant.complete();
        }

        int growthAfter = sharedPlant.getGrowthValue();
        int growthIncrement = growthAfter - growthBefore;
        boolean affectedGrowth = growthIncrement > 0;
        boolean usedNutrient = dto.supplyType() == SupplyType.NUTRIENT;

        SharedPlantLog log = SharedPlantLogConverter.toSharedPlantLog(
                sharedPlant,
                currentMember.getId(),
                affectedGrowth,
                growthIncrement,
                usedNutrient
        );

        sharedPlantLogRepository.save(log);

        return SharedPlantConverter.toWaterInfoResDTO(sharedPlant, currentMember);
    }

    @Override
    public SharedPlantResDTO.PlantActionResDTO water(Long sharedPlantId, Long memberId) {
        Member member = findMemberById(memberId);
        SharedPlant sharedPlant = findSharedPlantById(sharedPlantId);
        validateParticipant(sharedPlant, memberId);

        if (!sharedPlant.isPlanted()) {
            throw new SharedPlantException(SharedPlantErrorCode.NOT_PLANTED);
        }

        if (sharedPlant.getStatus() == SharedPlantStatus.COMPLETED) {
            throw new SharedPlantException(SharedPlantErrorCode.CANNOT_WATER_COMPLETED_PLANT);
        }


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

        if (sharedPlant.getIsSoloMode() && !memberId.equals(sharedPlant.getSoloPowerMemberId())) {
            sharedPlant.exitSoloMode();
        }

        if (sharedPlant.getLastWateredBy() != null && memberId.equals(sharedPlant.getLastWateredBy())) {
            throw new SharedPlantException(SharedPlantErrorCode.CANNOT_WATER_CONSECUTIVELY);
        }

        boolean isFirst = sharedPlant.getLastWateredAt() == null && sharedPlant.getLastWateredBy() == null && sharedPlant.getGrowthDate() == null;
        sharedPlant.water(GrowthType.WATER.getGrowth(), member, isFirst);

        if (wasWithered) {
            sharedPlant.revive();
        }

        if (sharedPlant.hasReachedMaxGrowth()) {
            sharedPlant.complete();
        }

        sharedPlantLogRepository.save(SharedPlantLogConverter.toSharedPlantLog(sharedPlant, memberId, true, GrowthType.WATER.getGrowth(), false));

        return plantActionResponseBuilder.build(sharedPlant, member);
    }

    @Override
    public SharedPlantResDTO.PlantActionResDTO plantSeed(Long sharedPlantId, Long memberId) {
        Member member = findMemberById(memberId);
        SharedPlant sharedPlant = findSharedPlantById(sharedPlantId);
        validateParticipant(sharedPlant, memberId);

        if (sharedPlant.isPlanted()) {
            throw new SharedPlantException(SharedPlantErrorCode.ALREADY_PLANTED);
        }

        sharedPlant.plant();

        return plantActionResponseBuilder.build(sharedPlant, member);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private SharedPlant findSharedPlantById(Long sharedPlantId) {
        return sharedPlantRepository.findById(sharedPlantId)
                .orElseThrow(() -> new SharedPlantException(SharedPlantErrorCode.SHARED_PLANT_NOT_FOUND));
    }

    private void validateParticipant(SharedPlant sharedPlant, Long memberId) {
        if (!(memberId.equals(sharedPlant.getMemberA().getId()) || memberId.equals(sharedPlant.getMemberB().getId()))) {
            throw new SharedPlantException(SharedPlantErrorCode.NOT_A_PARTICIPANT);
        }
    }
}
