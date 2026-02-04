package com.cotato.itda.domain.garden.service.query;

import com.cotato.itda.domain.garden.dto.res.PlantInviteResDTO;

public interface SharedPlantInviteQueryService {
    PlantInviteResDTO.PlantInviteCandidatesResDto getInviteCandidates(Long memberId);

    PlantInviteResDTO.MyPlantInviteListResDTO getMyPendingInvitations(Long memberId);
}
