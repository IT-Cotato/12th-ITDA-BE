package com.cotato.itda.domain.notification.service.command;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantInvite;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.notification.converter.NotificationConverter;
import com.cotato.itda.domain.notification.entity.Notification;
import com.cotato.itda.domain.notification.enums.NotificationSection;
import com.cotato.itda.domain.notification.enums.NotificationTargetType;
import com.cotato.itda.domain.notification.enums.NotificationType;
import com.cotato.itda.domain.notification.exception.NotificationException;
import com.cotato.itda.domain.notification.exception.code.NotificationErrorCode;
import com.cotato.itda.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandServiceImpl implements NotificationCommandService {

	private final NotificationRepository notificationRepository;

	@Override
	public void markAsRead(Long memberId, Long notificationId) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

		if (!notification.isOwnedBy(memberId)) {
			throw new NotificationException(NotificationErrorCode.NOTIFICATION_FORBIDDEN);
		}

		notification.markAsRead(LocalDateTime.now());
	}

	@Override
	public void markAllAsRead(Long memberId) {
		notificationRepository.markAllAsReadByReceiverId(memberId, LocalDateTime.now());
	}

	@Override
	public void createChallengeCommentNotification(Member actor, Challenge challenge) {
		createIfNotSelf(
			actor,
			challenge.getMember(),
			NotificationSection.DAILY_RECORD,
			NotificationType.CHALLENGE_COMMENT,
			actor.getName() + "님이 하루 한컷에 댓글을 남겼어요.",
			challenge.getImageUrl(),
			NotificationTargetType.CHALLENGE,
			challenge.getId()
		);
	}

	@Override
	public void createChallengeLikeNotification(Member actor, Challenge challenge) {
		createIfNotSelf(
			actor,
			challenge.getMember(),
			NotificationSection.DAILY_RECORD,
			NotificationType.CHALLENGE_LIKE,
			actor.getName() + "님이 하루 한컷을 좋아해요.",
			challenge.getImageUrl(),
			NotificationTargetType.CHALLENGE,
			challenge.getId()
		);
	}

	@Override
	public void createDiaryCommentNotification(Member actor, Diary diary) {
		createIfNotSelf(
			actor,
			diary.getMember(),
			NotificationSection.DAILY_RECORD,
			NotificationType.DIARY_COMMENT,
			actor.getName() + "님이 공유일기에 댓글을 남겼어요.",
			diary.getImageUrl(),
			NotificationTargetType.DIARY,
			diary.getId()
		);
	}

	@Override
	public void createDiaryLikeNotification(Member actor, Diary diary) {
		createIfNotSelf(
			actor,
			diary.getMember(),
			NotificationSection.DAILY_RECORD,
			NotificationType.DIARY_LIKE,
			actor.getName() + "님이 공유일기를 좋아해요.",
			diary.getImageUrl(),
			NotificationTargetType.DIARY,
			diary.getId()
		);
	}

	@Override
	public void createPlantInviteNotification(Member actor, SharedPlantInvite invite) {
		createIfNotSelf(
			actor,
			invite.getInvitee(),
			NotificationSection.GARDEN,
			NotificationType.PLANT_INVITE,
			actor.getName() + "님이 초대장을 보냈어요.",
			null,
			NotificationTargetType.SHARED_PLANT_INVITE,
			invite.getId()
		);
	}

	@Override
	public void createPlantInviteAcceptedNotification(Member actor, SharedPlantInvite invite, SharedPlant sharedPlant) {
		createIfNotSelf(
			actor,
			invite.getInviter(),
			NotificationSection.GARDEN,
			NotificationType.PLANT_INVITE_ACCEPTED,
			actor.getName() + "님이 초대장을 수락했어요.",
			null,
			NotificationTargetType.SHARED_PLANT,
			sharedPlant.getId()
		);
	}

	@Override
	public void createPlantWaterNeededNotification(Member receiver, SharedPlant sharedPlant) {
		if (sharedPlant.getLastWateredAt() == null
			|| LocalDateTime.now().isBefore(sharedPlant.getLastWateredAt().plusHours(24))
			|| alreadySentAfterLastWatering(receiver, sharedPlant)) {
			return;
		}

		create(
			receiver,
			NotificationSection.GARDEN,
			NotificationType.PLANT_WATER_NEEDED,
			sharedPlant.getNickname() + "에 물이 필요해요.",
			null,
			NotificationTargetType.SHARED_PLANT,
			sharedPlant.getId()
		);
	}

	private boolean alreadySentAfterLastWatering(Member receiver, SharedPlant sharedPlant) {
		return notificationRepository.existsByReceiverIdAndTypeAndTargetTypeAndTargetIdAndCreatedAtAfter(
			receiver.getId(),
			NotificationType.PLANT_WATER_NEEDED,
			NotificationTargetType.SHARED_PLANT,
			sharedPlant.getId(),
			sharedPlant.getLastWateredAt()
		);
	}

	private void createIfNotSelf(
		Member actor,
		Member receiver,
		NotificationSection section,
		NotificationType type,
		String content,
		String imageUrl,
		NotificationTargetType targetType,
		Long targetId
	) {
		if (actor.getId().equals(receiver.getId())) {
			return;
		}

		create(receiver, section, type, content, imageUrl, targetType, targetId);
	}

	private void create(
		Member receiver,
		NotificationSection section,
		NotificationType type,
		String content,
		String imageUrl,
		NotificationTargetType targetType,
		Long targetId
	) {
		notificationRepository.save(NotificationConverter.toNotification(
			receiver,
			section,
			type,
			content,
			imageUrl,
			targetType,
			targetId
		));
	}
}
