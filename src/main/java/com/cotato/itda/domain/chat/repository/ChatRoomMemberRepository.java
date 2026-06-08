package com.cotato.itda.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cotato.itda.domain.chat.entity.ChatRoomMember;
import com.cotato.itda.domain.chat.enums.MemberRoomStatus;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

	// 특정 방에 특정 멤버가 특정 상태로 참여 중인지 확인
	// ex: 이 사용자가 이 방에 ACTIVE로  속해 있나?
	@Query("""
		    select crm
		    from ChatRoomMember crm
		    where crm.room.id = :roomId
		      and crm.member.id = :memberId
		      and crm.status = :status
		""")
	Optional<ChatRoomMember> findOneByRoomMemberStatus(
		@Param("roomId") Long roomId,
		@Param("memberId") Long memberId,
		@Param("status") MemberRoomStatus status
	);

	Optional<ChatRoomMember> findByRoomIdAndMemberId(Long roomId, Long memberId);

	// 특정 roomId의 멤버 중 내가 아닌 멤버(상대)를 찾는 쿼리
	// DIRECT에서만 사용
	@Query("""
			select crm.member.id
			from ChatRoomMember crm
			where crm.room.id = :roomId
				and crm.member.id <> :myMemberId
				and crm.status = :status
		""")
	Optional<Long> findOpponentMemberId(
		@Param("roomId") Long roomId,
		@Param("myMemberId") Long myMemberId,
		@Param("status") MemberRoomStatus status
	);

	@Query("""
			select crm.room.id
			from ChatRoomMember crm
			where crm.member.id = :memberId
			  and crm.status = :status
			  and crm.room.id in :roomIds
		""")
	List<Long> findActiveRoomIdsIn(Long memberId,
		@Param("status") MemberRoomStatus status,
		@Param("roomIds") List<Long> roomIds
	);

    List<ChatRoomMember> findAllByRoomId(Long roomId);
}
