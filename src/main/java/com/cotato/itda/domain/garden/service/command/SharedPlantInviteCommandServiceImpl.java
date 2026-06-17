package com.cotato.itda.domain.garden.service.command;

import com.cotato.itda.domain.friendship.exception.FriendshipException;
import com.cotato.itda.domain.friendship.exception.code.FriendshipErrorCode;
import com.cotato.itda.domain.friendship.repository.FriendshipRepository;
import com.cotato.itda.domain.garden.converter.SharedPlantConverter;
import com.cotato.itda.domain.garden.converter.SharedPlantInviteConverter;
import com.cotato.itda.domain.garden.dto.req.PlantInviteReqDTO;
import com.cotato.itda.domain.garden.dto.res.PlantInviteResDTO;
import com.cotato.itda.domain.garden.entity.Plant;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantInvite;
import com.cotato.itda.domain.garden.enums.InviteStatus;
import com.cotato.itda.domain.garden.exception.PlantException;
import com.cotato.itda.domain.garden.exception.SharedPlantException;
import com.cotato.itda.domain.garden.exception.SharedPlantInviteException;
import com.cotato.itda.domain.garden.exception.code.PlantErrorCode;
import com.cotato.itda.domain.garden.exception.code.SharedPlantErrorCode;
import com.cotato.itda.domain.garden.exception.code.SharedPlantInviteErrorCode;
import com.cotato.itda.domain.garden.repository.PlantRepository;
import com.cotato.itda.domain.garden.repository.SharedPlantInviteRepository;
import com.cotato.itda.domain.garden.repository.SharedPlantRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.domain.notification.service.command.NotificationCommandService;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedPlantInviteCommandServiceImpl implements SharedPlantInviteCommandService {

    private final MemberRepository memberRepository;
    private final PlantRepository plantRepository;
    private final FriendshipRepository friendshipRepository;
    private final SharedPlantRepository sharedPlantRepository;
    private final SharedPlantInviteRepository sharedPlantInviteRepository;
    private final NotificationCommandService notificationCommandService;

    @Override
    public PlantInviteResDTO.CreateSharedPlantInviteResDTO createInvite(Long inviterId,
            PlantInviteReqDTO.CreateSharedPlantInviteReqDTO dto) {

        if (inviterId.equals(dto.inviteeId())) {
            throw new SharedPlantInviteException(SharedPlantInviteErrorCode.SELF_INVITE_NOT_ALLOWED);
        }

        // 1. 초대받는 유저 및 식물의 존재 여부 확인
        Member inviter = memberRepository.findById(inviterId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Member invitee = memberRepository.findById(dto.inviteeId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Plant plant = plantRepository.findByName(dto.plantName())
                .orElseThrow(() -> new PlantException(PlantErrorCode.NOT_FOUND));

        // 2. 두 유저가 활성 친구(ACTIVE) 상태인지 확인
        if (!friendshipRepository.existsByMember_IdAndFriend_Id(inviterId, dto.inviteeId())) {
            throw new FriendshipException(FriendshipErrorCode.NOT_ACTIVE);
        }

        // 3. 이미 대기 중인 초대(PENDING)가 있는지 확인
        if (sharedPlantInviteRepository.existsPendingInviteBetween(inviterId, dto.inviteeId())) {
            throw new SharedPlantInviteException(SharedPlantInviteErrorCode.ALREADY_HAS_PENDING_INVITE);
        }

        // 4. 이미 함께 키우는 중(GROWING, WITHERED)인지 확인
        // memberAId < memberBId
        Long memberAId = Math.min(inviterId, dto.inviteeId());
        Long memberBId = Math.max(inviterId, dto.inviteeId());
        if (sharedPlantRepository.existsGrowingOrWitheredBetween(memberAId, memberBId)) {
            throw new SharedPlantException(SharedPlantErrorCode.ALREADY_HAS_SHARED_PLANT);
        }

        // 5. SharedPlantInvite 생성 및 저장
        SharedPlantInvite invite = SharedPlantInvite.builder()
                .inviter(inviter)
                .invitee(invitee)
                .plant(plant)
                .nickname(dto.nickname())
                .message(dto.message())
                .status(InviteStatus.PENDING)
                .build();

        SharedPlantInvite savedInvite = sharedPlantInviteRepository.save(invite);
        notificationCommandService.createPlantInviteNotification(inviter, savedInvite);

        return SharedPlantInviteConverter.toCreateSharedPlantInviteResDTO(savedInvite);
    }

    @Override
    public PlantInviteResDTO.UpdateSharedPlantInviteResDTO updateInviteStatus(
            Long inviteeId, Long inviteId,
            PlantInviteReqDTO.UpdateSharedPlantInviteReqDTO dto
    ) {

        // 1. 식물 초대 존재 여부 확인
        SharedPlantInvite invite = sharedPlantInviteRepository.findById(inviteId)
                .orElseThrow(() -> new SharedPlantInviteException(SharedPlantInviteErrorCode.INVITE_NOT_FOUND));

        // 2. 요청을 보낸 사람이 초대받은 사람인지 확인
        if (!invite.getInvitee().getId().equals(inviteeId)) {
            throw new SharedPlantInviteException(SharedPlantInviteErrorCode.NOT_INVITEE);
        }

        // 3. 초대 상태가 PENDING인지 확인
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new SharedPlantInviteException(SharedPlantInviteErrorCode.INVITE_NOT_PENDING);
        }

        // 4. 수락 혹은 거절 상태여야 함 (PENDING으로 다시 변경 불가)
        if (dto.status() == InviteStatus.PENDING) {
            throw new SharedPlantInviteException(SharedPlantInviteErrorCode.INVALID_INVITE_STATUS);
        }

        SharedPlant sharedPlant = null;

        // 5. status가 ACCEPTED라면 현재 키우고 있는 식물이 있는지 확인 후 새 식물을 만들기
        if (dto.status() == InviteStatus.ACCEPTED) {
            // memberAId < memberBId
            Long memberAId = Math.min(invite.getInviter().getId(), invite.getInvitee().getId());
            Long memberBId = Math.max(invite.getInviter().getId(), invite.getInvitee().getId());

            if (sharedPlantRepository.existsGrowingOrWitheredBetween(memberAId, memberBId)) {
                throw new SharedPlantException(SharedPlantErrorCode.ALREADY_HAS_SHARED_PLANT);
            }

            sharedPlant = SharedPlantConverter.toSharedPlant(invite);

            sharedPlantRepository.save(sharedPlant);
        }

        invite.updateStatus(dto.status());
        if (sharedPlant != null) {
            notificationCommandService.createPlantInviteAcceptedNotification(invite.getInvitee(), invite, sharedPlant);
        }

        return SharedPlantInviteConverter.toUpdateSharedPlantInviteResDTO(invite, sharedPlant);
    }
}
