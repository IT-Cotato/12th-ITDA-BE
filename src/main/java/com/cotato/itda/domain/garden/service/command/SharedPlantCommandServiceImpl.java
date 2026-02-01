package com.cotato.itda.domain.garden.service.command;

import com.cotato.itda.domain.garden.converter.SharedPlantConverter;
import com.cotato.itda.domain.garden.converter.SharedPlantLogConverter;
import com.cotato.itda.domain.garden.dto.req.SharedPlantReqDTO;
import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantLog;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.cotato.itda.domain.garden.enums.SupplyType;
import com.cotato.itda.domain.garden.exception.SharedPlantException;
import com.cotato.itda.domain.garden.exception.code.SharedPlantErrorCode;
import com.cotato.itda.domain.garden.repository.SharedPlantLogRepository;
import com.cotato.itda.domain.garden.repository.SharedPlantRepository;
import com.cotato.itda.domain.garden.service.command.strategy.WateringStrategy;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedPlantCommandServiceImpl implements SharedPlantCommandService {

    private final List<WateringStrategy> wateringStrategies;
    private final SharedPlantRepository sharedPlantRepository;
    private final MemberRepository memberRepository;
    private final SharedPlantLogRepository sharedPlantLogRepository;

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

        if (sharedPlant.getIsSoloMode() && !sharedPlant.getSoloPowerMemberId().equals(currentMemberId)) {
            sharedPlant.exitSoloMode();
        }

        int growthBefore = sharedPlant.getGrowthValue();

        WateringStrategy strategy = wateringStrategies.stream()
                .filter(s -> s.supports(sharedPlant, dto))
                .findFirst()
                .orElseThrow(() -> new SharedPlantException(SharedPlantErrorCode.NOT_FOUND_STRATEGY));

        strategy.water(sharedPlant, currentMember, wateringStrategies);

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
}
