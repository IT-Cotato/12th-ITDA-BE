package com.cotato.itda.domain.friendship.service.command;

import com.cotato.itda.domain.friendship.converter.FriendshipConverter;
import com.cotato.itda.domain.friendship.dto.req.FriendshipReqDTO;
import com.cotato.itda.domain.friendship.dto.res.FriendshipResDTO;
import com.cotato.itda.domain.chattopic.entity.ChatTopic;
import com.cotato.itda.domain.chattopic.exception.ChatTopicException;
import com.cotato.itda.domain.chattopic.exception.code.ChatTopicErrorCode;
import com.cotato.itda.domain.chattopic.repository.ChatTopicRepository;
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
                List<ChatTopic> requestedTopics = validateAndFetchTopics(topicCodes);
                friendship.replaceTopics(requestedTopics);
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
