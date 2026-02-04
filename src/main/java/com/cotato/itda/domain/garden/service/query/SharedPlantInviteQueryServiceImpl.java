package com.cotato.itda.domain.garden.service.query;

import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.repository.FriendshipRepository;
import com.cotato.itda.domain.garden.dto.res.PlantInviteResDTO;
import com.cotato.itda.domain.garden.repository.SharedPlantInviteRepository;
import com.cotato.itda.domain.garden.repository.SharedPlantRepository;
import com.cotato.itda.domain.garden.converter.SharedPlantInviteConverter;
import com.cotato.itda.domain.garden.entity.SharedPlantInvite;
import com.cotato.itda.domain.garden.enums.InviteStatus;
import com.cotato.itda.domain.member.entity.MemberStatus;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedPlantInviteQueryServiceImpl implements SharedPlantInviteQueryService {

        private final FriendshipRepository friendshipRepository;
        private final SharedPlantRepository sharedPlantRepository;
        private final SharedPlantInviteRepository sharedPlantInviteRepository;
        private final MemberRepository memberRepository;

        @Override
        public PlantInviteResDTO.PlantInviteCandidatesResDto getInviteCandidates(Long memberId) {

                // 1. friendship 도메인에서 ACTIVE 상태인 친구 목록 및 ID 가져오기
                List<Friendship> totalFriends = friendshipRepository.findAllByMemberIdAndStatus(
                                memberId, FriendshipStatus.ACTIVE,
                                Sort.by(Sort.Direction.DESC, "lastInteractedAt"));

                // 2. shared_plant 도메인에서 현재 나와 GROWING, WITHERED 상태의 shared_plant가 있는 친구 ID 목록
                // 조회
                List<Long> growingFriendIds = sharedPlantRepository
                                .findGrowingOrWitheredSharedPlantFriendIds(memberId);

                // 3. shared_plant_invite 테이블에서 현재 나와 PENDING 상태인 친구 ID 목록 조회
                List<Long> pendingInviteFriendIds = sharedPlantInviteRepository.findPendingInviteFriendIds(memberId);

                // 4. 전체 친구 리스트에서 2번과 3번 ID를 제외 (차집합) + 멤버 상태가 ACTIVE인 경우만
                List<Friendship> filteredFriends = totalFriends.stream()
                                .filter(friendship -> friendship.getFriend().getStatus() == MemberStatus.ACTIVE)
                                .filter(friendship -> !growingFriendIds.contains(friendship.getFriend().getId()))
                                .filter(friendship -> !pendingInviteFriendIds.contains(friendship.getFriend().getId()))
                                .toList();

                return SharedPlantInviteConverter.toPlantInviteCandidatesResDto(filteredFriends);
        }

        @Override
        public PlantInviteResDTO.MyPlantInviteListResDTO getMyPendingInvitations(Long memberId) {

                // 0. 사용자 존재 여부 확인
                if (!memberRepository.existsById(memberId)) {
                        throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
                }

                // 1. 내가 받은 PENDING 상태의 초대 목록 내림차순 조회 (오래된 순)
                List<SharedPlantInvite> invitations = sharedPlantInviteRepository
                                .findAllByInviteeIdAndStatusOrderByCreatedAtAsc(memberId, InviteStatus.PENDING);

                if (invitations.isEmpty()) {
                        return SharedPlantInviteConverter.toMyPlantInviteListResDTO(invitations, List.of());
                }

                // 2. 초대자들의 표시 이름(DisplayName)을 가져오기 위해 필요한 친구 관계만 조회
                List<Long> inviterIds = invitations.stream()
                                .map(invite -> invite.getInviter().getId())
                                .distinct()
                                .toList();

                List<Friendship> friendships = friendshipRepository.findAllByMemberIdAndFriendIdInAndStatus(
                                memberId, inviterIds, FriendshipStatus.ACTIVE);

                return SharedPlantInviteConverter.toMyPlantInviteListResDTO(invitations, friendships);
        }
}
