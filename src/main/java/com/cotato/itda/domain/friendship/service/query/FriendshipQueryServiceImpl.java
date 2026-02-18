package com.cotato.itda.domain.friendship.service.query;

import com.cotato.itda.domain.friendship.converter.FriendshipConverter;
import com.cotato.itda.domain.friendship.dto.res.FriendshipResDTO;
import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.exception.FriendshipException;
import com.cotato.itda.domain.friendship.exception.code.FriendshipErrorCode;
import com.cotato.itda.domain.friendship.repository.FriendshipRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import com.cotato.itda.domain.member.entity.MemberStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendshipQueryServiceImpl implements FriendshipQueryService {

    private final FriendshipRepository friendshipRepository;
    private final MemberRepository memberRepository;

    @Override
    public FriendshipResDTO.FriendshipListDTO getFriendshipList(
            Long memberId,
            List<FriendshipStatus> statuses,
            Sort sort) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND,
                    Map.of("memberId", memberId));
        }

        if (statuses == null || statuses.isEmpty()) {
            statuses = List.of(FriendshipStatus.ACTIVE);
        }

        List<Friendship> friendships = friendshipRepository
                .findAllByMemberIdAndStatusIn(memberId, statuses, sort);

        // 상대방 친구의 계정 상태가 ACTIVE인 경우만 필터링
        List<Friendship> filteredFriendships = friendships.stream()
                .filter(friendship -> friendship.getFriend().getStatus() == MemberStatus.ACTIVE)
                .toList();

        return FriendshipConverter.toFriendshipListDTO(filteredFriendships);
    }

    @Override
    public FriendshipResDTO.FriendshipSettingsDTO getFriendshipSettings(Long friendshipId, Long memberId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipException(FriendshipErrorCode.NOT_FOUND,
                        Map.of("friendshipId", friendshipId)));

        // 본인의 친구 관계인지 확인
        if (!friendship.getMember().getId().equals(memberId)) {
            throw new FriendshipException(FriendshipErrorCode.FORBIDDEN,
                    Map.of("friendshipId", friendshipId, "memberId", memberId));
        }

        return FriendshipConverter.toFriendshipSettingsDTO(friendship);
    }

    @Override
    public FriendshipResDTO.SearchByInviteCodeDTO searchByInviteCode(Long memberId, String inviteCode) {
        Member member = memberRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new FriendshipException(FriendshipErrorCode.MEMBER_NOT_FOUND_BY_INVITE_CODE));

        if (member.getId().equals(memberId)) {
            throw new FriendshipException(FriendshipErrorCode.CANNOT_SEARCH_SELF);
        }

        return FriendshipConverter.toSearchByInviteCodeDTO(member);
    }
}
