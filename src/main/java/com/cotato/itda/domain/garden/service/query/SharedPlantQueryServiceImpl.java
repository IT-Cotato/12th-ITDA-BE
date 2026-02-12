package com.cotato.itda.domain.garden.service.query;

import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.repository.FriendshipRepository;
import com.cotato.itda.domain.garden.converter.SharedPlantConverter;
import com.cotato.itda.domain.garden.dto.SharedPlantWithFriendship;
import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.enums.GardenState;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.cotato.itda.domain.garden.repository.SharedPlantRepository;
import com.cotato.itda.domain.garden.service.GardenStateCalculator;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedPlantQueryServiceImpl implements SharedPlantQueryService {

    private final SharedPlantRepository sharedPlantRepository;
    private final FriendshipRepository friendshipRepository;
    private final MemberRepository memberRepository;
    private final GardenStateCalculator gardenStateCalculator;

    @Override
    public SharedPlantResDTO.SharedPlantInfoListDTO getSharedPlants(Long memberId, List<SharedPlantStatus> statuses) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        // 1. 상태에 해당하는 공유 식물 조회
        List<SharedPlant> sharedPlants = sharedPlantRepository.findAllByMemberIdAndStatusIn(memberId, statuses);

        if (sharedPlants.isEmpty()) {
            return SharedPlantResDTO.SharedPlantInfoListDTO.builder()
                    .totalCount(0)
                    .nutrientCount(member.getNutrientCount())
                    .sharedPlants(List.of())
                    .build();
        }

        // 2. 각 공유 식물에서 친구 ID 추출
        List<Long> friendIds = sharedPlants.stream()
                .map(sp -> sp.getMemberA().getId().equals(memberId)
                        ? sp.getMemberB().getId()
                        : sp.getMemberA().getId())
                .toList();

        // 3. Friendship 조회 후 Map으로 변환
        List<Friendship> friendships = friendshipRepository.findAllByMember_IdAndFriend_IdInAndStatus(
                memberId, friendIds, FriendshipStatus.ACTIVE);

        Map<Long, Friendship> friendshipMap = friendships.stream()
                .collect(Collectors.toMap(Friendship::getFriendId, f -> f));

        // 4. SharedPlant와 Friendship 짝짓기
        List<SharedPlantWithFriendship> pairs = sharedPlants.stream()
                .map(sp -> {
                    Long friendId = sp.getMemberA().getId().equals(memberId)
                            ? sp.getMemberB().getId()
                            : sp.getMemberA().getId();
                    return new SharedPlantWithFriendship(sp, friendshipMap.get(friendId));
                })
                .toList();

        // 5. DTO 변환 (gardenState, percentage 계산 포함)
        List<SharedPlantResDTO.SharedPlantInfoDTO> sharedPlantInfoDTOs = pairs.stream()
                .filter(pair -> pair.friendship() != null)
                .map(pair -> {
                    SharedPlant sp = pair.sharedPlant();
                    GardenState gardenState = gardenStateCalculator.calculateState(sp);
                    int percentage = gardenStateCalculator.calculatePercentage(sp);
                    return SharedPlantConverter.toSharedPlantInfoDTO(
                            sp, pair.friendship(), memberId, gardenState, percentage
                    );
                })
                .toList();

        return SharedPlantConverter.toSharedPlantInfoListDTO(sharedPlantInfoDTOs, member.getNutrientCount());
    }
}
