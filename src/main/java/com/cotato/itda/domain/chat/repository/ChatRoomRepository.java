package com.cotato.itda.domain.chat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cotato.itda.domain.chat.entity.ChatRoom;
import com.cotato.itda.domain.chat.enums.RoomType;

import jakarta.persistence.LockModeType;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	/**
	 * message_seq를 안전하게  증가시키려면 room row를 잠그고(lastMessageSeq 기반) 다음 seq를 계산해야 한다
	 * - 비관적 락(PESSIMISTIC_WRITE): 동시에 여러 SEND 요청이 들어와도 seq가 꼬이지 않게 막아줌
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from ChatRoom r where r.id= :roomId")
	Optional<ChatRoom> findByIdForUpdate(Long roomId);

	// ChatRoom을 JPQL로 조회하기 위한 Repository 메서드
	// 목적: DIRECT(1:1) 채팅방이 이미 존재하는지 확인(Resolve)해서,
	//       있으면 기존 방을 재사용하고, 없으면 새로 만들 때 사용한다.
	@Query("""
		select r
		from ChatRoom r
		where r.roomType = :roomType
		and r.directMemberLowId = :directMemberLowId
		and r.directMemberHighId = :directMemberHighId
		""")
	Optional<ChatRoom> findByRoomTypeAndDirectMemberLowIdAndDirectMemberHighId(
		@Param("roomType") RoomType roomType,                 // 방 타입 (보통 DIRECT)
		@Param("directMemberLowId") Long directMemberLowId,   // 두 멤버 중 더 작은 id (min)
		@Param("directMemberHighId") Long directMemberHighId  // 두 멤버 중 더 큰 id (max)
	);
}
