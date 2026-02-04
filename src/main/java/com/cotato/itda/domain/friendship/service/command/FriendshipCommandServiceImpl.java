package com.cotato.itda.domain.friendship.service.command;

import com.cotato.itda.domain.friendship.converter.FriendshipConverter;
import com.cotato.itda.domain.friendship.dto.req.FriendshipReqDTO;
import com.cotato.itda.domain.friendship.dto.res.FriendshipResDTO;
import com.cotato.itda.domain.chattopic.entity.ChatTopic;
import com.cotato.itda.domain.chattopic.exception.ChatTopicException;
import com.cotato.itda.domain.chattopic.exception.code.ChatTopicErrorCode;
import com.cotato.itda.domain.chattopic.repository.ChatTopicRepository;
import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.friendship.entity.mapping.FriendshipTopic;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.exception.FriendshipException;
import com.cotato.itda.domain.friendship.exception.code.FriendshipErrorCode;
import com.cotato.itda.domain.friendship.repository.FriendshipRepository;
import com.cotato.itda.domain.friendship.repository.FriendshipTopicRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendshipCommandServiceImpl implements FriendshipCommandService {

        private final FriendshipRepository friendshipRepository;
        private final MemberRepository memberRepository;
        private final ChatTopicRepository chatTopicRepository;
        private final FriendshipTopicRepository friendshipTopicRepository;

        @Transactional
        @Override
        public FriendshipResDTO.CreateDTO createFriendship(Long friendId, Long memberId) {

                if (memberId.equals(friendId)) {
                        throw new FriendshipException(
                                        FriendshipErrorCode.CANNOT_ADD_SELF);
                }

                if (friendshipRepository.existsByMember_IdAndFriend_Id(memberId, friendId)) {
                        throw new FriendshipException(
                                        FriendshipErrorCode.FRIENDSHIP_ALREADY_EXISTS);
                }

                Member member = memberRepository.findById(memberId)
                                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND,
                                                Map.of("userId", memberId)));

                Member friend = memberRepository.findById(friendId)
                                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND,
                                                Map.of("friendId", friendId)));

                Friendship friendship = FriendshipConverter.toFriendship(member, friend);

                Friendship savedFriendship = friendshipRepository.save(friendship);

                return FriendshipConverter.toCreateDTO(savedFriendship);
        }

        @Transactional
        @Override
        public FriendshipResDTO.UpdateDTO updateFriendship(FriendshipReqDTO.UpdateDTO dto, Long friendshipId,
                        Long memberId) {

                Friendship friendship = friendshipRepository.findById(friendshipId)
                                .orElseThrow(() -> new FriendshipException(FriendshipErrorCode.NOT_FOUND,
                                                Map.of("friendshipId", friendshipId)));

                // 본인의 친구 관계인지 확인
                if (!friendship.getMember().getId().equals(memberId)) {
                        throw new FriendshipException(FriendshipErrorCode.FORBIDDEN,
                                        Map.of("friendshipId", friendshipId, "memberId", memberId));
                }

                friendship.update(dto.nickname(), dto.speechStyle(), dto.chatGoal(), FriendshipStatus.ACTIVE);

                updateFriendshipTopics(friendship, dto.topicCodes());

                return FriendshipConverter.toUpdateDTO(friendship);
        }

        private void updateFriendshipTopics(Friendship friendship, List<String> topicCodes) {
                // 1. 요청한 주제 검증 및 조회
                List<ChatTopic> requestedTopics = validateAndFetchTopics(topicCodes);

                // 2. 기존 주제 조회
                List<FriendshipTopic> existingTopics = friendship.getFriendshipTopics();

                // 3. 변경분 계산 및 처리
                updateTopicChanges(friendship, requestedTopics, existingTopics);
        }

        private List<ChatTopic> validateAndFetchTopics(List<String> topicCodes) {
                List<ChatTopic> topics = chatTopicRepository.findAllByCodeIn(topicCodes);

                Set<String> foundCodes = topics.stream()
                                .map(ChatTopic::getCode)
                                .collect(Collectors.toSet());

                List<String> notFoundCodes = topicCodes.stream()
                                .filter(code -> !foundCodes.contains(code))
                                .toList();

                if (!notFoundCodes.isEmpty()) {
                        throw new ChatTopicException(
                                        ChatTopicErrorCode.BAD_REQUEST,
                                        Map.of("notFoundCodes", notFoundCodes, "requestedCodes", topicCodes));
                }

                return topics;
        }

        private void updateTopicChanges(
                        Friendship friendship,
                        List<ChatTopic> requestedTopics,
                        List<FriendshipTopic> existingTopics) {
                Set<String> requestedCodes = requestedTopics.stream()
                                .map(ChatTopic::getCode)
                                .collect(Collectors.toSet());

                Set<String> existingCodes = existingTopics.stream()
                                .map(ft -> ft.getChatTopic().getCode())
                                .collect(Collectors.toSet());

                // 삭제할 항목 (기존에 있었는데 요청에 없는 것)
                List<FriendshipTopic> topicsToDelete = existingTopics.stream()
                                .filter(ft -> !requestedCodes.contains(ft.getChatTopic().getCode()))
                                .toList();

                // 추가할 항목 (요청에 있는데 기존에 없는 것)
                List<FriendshipTopic> topicsToAdd = requestedTopics.stream()
                                .filter(topic -> !existingCodes.contains(topic.getCode()))
                                .map(topic -> FriendshipTopic.builder()
                                                .friendship(friendship)
                                                .chatTopic(topic)
                                                .build())
                                .toList();

                // 저장
                if (!topicsToDelete.isEmpty()) {
                        friendshipTopicRepository.deleteAll(topicsToDelete);
                }
                if (!topicsToAdd.isEmpty()) {
                        friendshipTopicRepository.saveAll(topicsToAdd);
                }
        }

        @Override
        @Transactional
        public void deleteFriendship(Long friendshipId, Long memberId) {
                Friendship friendship = friendshipRepository.findById(friendshipId)
                                .orElseThrow(() -> new FriendshipException(FriendshipErrorCode.NOT_FOUND,
                                                Map.of("friendshipId", friendshipId)));

                // 본인의 친구 관계인지 확인
                if (!friendship.getMember().getId().equals(memberId)) {
                        throw new FriendshipException(FriendshipErrorCode.FORBIDDEN,
                                        Map.of("friendshipId", friendshipId, "memberId", memberId));
                }

                friendshipRepository.delete(friendship);
        }
}
