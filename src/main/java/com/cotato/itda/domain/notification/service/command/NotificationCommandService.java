package com.cotato.itda.domain.notification.service.command;

import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantInvite;
import com.cotato.itda.domain.member.entity.Member;

public interface NotificationCommandService {

	void markAsRead(Long memberId, Long notificationId);

	void markAllAsRead(Long memberId);

	void createChallengeCommentNotification(Member actor, Challenge challenge);

	void createChallengeLikeNotification(Member actor, Challenge challenge);

	void createDiaryCommentNotification(Member actor, Diary diary);

	void createDiaryLikeNotification(Member actor, Diary diary);

	void createPlantInviteNotification(Member actor, SharedPlantInvite invite);

	void createPlantInviteAcceptedNotification(Member actor, SharedPlantInvite invite, SharedPlant sharedPlant);

	void createPlantWaterNeededNotification(Member receiver, SharedPlant sharedPlant);
}
