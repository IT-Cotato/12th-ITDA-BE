package com.cotato.itda.domain.garden.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.cotato.itda.domain.garden.repository.SharedPlantRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.notification.service.command.NotificationCommandService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlantWaterNeededNotificationScheduler {

	private final SharedPlantRepository sharedPlantRepository;
	private final NotificationCommandService notificationCommandService;

	// 물주기 가능 시점부터 시듦까지 여유가 있어 hourly polling으로 충분하다.
	@Scheduled(cron = "0 0 * * * *")
	@Transactional
	public void sendWaterNeededNotifications() {
		List<SharedPlant> plants = sharedPlantRepository
			.findAllByStatusAndIsPlantedTrueWithMembers(SharedPlantStatus.GROWING);

		for (SharedPlant plant : plants) {
			notifyWaterNeeded(plant);
		}
	}

	private void notifyWaterNeeded(SharedPlant plant) {
		Long lastWateredBy = plant.getLastWateredBy();

		if (lastWateredBy == null) {
			return;
		}

		Member receiver = lastWateredBy.equals(plant.getMemberA().getId())
			? plant.getMemberB()
			: plant.getMemberA();

		notificationCommandService.createPlantWaterNeededNotification(receiver, plant);
	}
}
