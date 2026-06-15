package com.cotato.itda.domain.notification.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cotato.itda.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByReceiverIdOrderByIdDesc(Long receiverId, Pageable pageable);

	List<Notification> findByReceiverIdAndIdLessThanOrderByIdDesc(Long receiverId, Long lastId, Pageable pageable);

	long countByReceiverIdAndReadFalse(Long receiverId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update Notification n
		set n.read = true,
			n.readAt = :readAt
		where n.receiver.id = :receiverId
			and n.read = false
		""")
	int markAllAsReadByReceiverId(@Param("receiverId") Long receiverId, @Param("readAt") LocalDateTime readAt);
}
