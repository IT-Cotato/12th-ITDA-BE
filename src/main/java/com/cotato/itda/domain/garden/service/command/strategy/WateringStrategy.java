package com.cotato.itda.domain.garden.service.command.strategy;

import com.cotato.itda.domain.garden.dto.req.SharedPlantReqDTO;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.member.entity.Member;

import java.util.List;

public interface WateringStrategy {

    /**
     * 이 전략이 현재 요청을 처리해야 하는지 판단합니다.
     * @param sharedPlant 대상 식물
     * @param dto 요청 DTO
     * @return 처리해야 하면 true
     */
    boolean supports(SharedPlant sharedPlant, SharedPlantReqDTO.WaterPlantReqDTO dto);

    /**
     * 실제 물 주기 또는 추가 기능 로직을 실행합니다.
     * @param sharedPlant 대상 식물
     * @param currentMember 요청한 회원
     * @param allStrategies 데코레이터가 기본 전략을 찾기 위해 필요한 모든 전략 리스트
     */
    void water(SharedPlant sharedPlant, Member currentMember, List<WateringStrategy> allStrategies);
}
