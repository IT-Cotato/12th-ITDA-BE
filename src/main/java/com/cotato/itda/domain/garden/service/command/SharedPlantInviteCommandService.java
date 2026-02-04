package com.cotato.itda.domain.garden.service.command;

import com.cotato.itda.domain.garden.dto.req.PlantInviteReqDTO;
import com.cotato.itda.domain.garden.dto.res.PlantInviteResDTO;

public interface SharedPlantInviteCommandService {
    PlantInviteResDTO.CreateSharedPlantInviteResDTO createInvite(Long inviterId,
            PlantInviteReqDTO.CreateSharedPlantInviteReqDTO dto);

    PlantInviteResDTO.UpdateSharedPlantInviteResDTO updateInviteStatus(Long inviteeId, Long inviteId,
            PlantInviteReqDTO.UpdateSharedPlantInviteReqDTO dto);
}
