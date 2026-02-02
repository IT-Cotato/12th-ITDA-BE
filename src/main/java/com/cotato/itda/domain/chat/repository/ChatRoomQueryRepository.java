package com.cotato.itda.domain.chat.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.cotato.itda.domain.chat.entity.QChatRoom;
import com.cotato.itda.domain.chat.entity.QChatRoomMember;
import com.cotato.itda.domain.chat.enums.MemberRoomStatus;
import com.cotato.itda.domain.chat.repository.dto.MyRoomRow;
import com.cotato.itda.domain.chat.repository.dto.OpponentRow;
import com.cotato.itda.domain.member.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatRoomQueryRepository {
	private final JPAQueryFactory queryFactory;

	/**
	 * 내가 속한 방 목록(메시지 있는 방만) + 커서 페이지네이션 + limit+1
	 */
	public List<MyRoomRow> findMyRoomsSlice(
		Long memberId,
		LocalDateTime cursorAt,
		Long cursorRoomId,
		int limitPlusOne
	) {
		// Querydsl이 생성한 Q타입 사용 예시
		// Q타입이란 Querydsl이 엔티티를 기반으로 생성한 클래스들
		// Q타입은 Querydsl이 엔티티를 보고 컴파일 때 자동 생성한다
		// 새 클래스를 만드는 이유는 JpaRepository가 못 하는 복잡 조회(조인/동적 where/커서 페이징/DTO 프로젝션)를 깔끔하게 하기 위해서
		QChatRoomMember chatRoomMember = QChatRoomMember.chatRoomMember;
		QChatRoom chatRoom = QChatRoom.chatRoom;

		// 동적 where 조건 빌더
		// BooleanBuilder는 조건을 계속 추가(AND/OR)해서 최종 where 조건식을 만드는 역할
		BooleanBuilder where = new BooleanBuilder();

		// 1) 내가 속한 방
		where.and(chatRoomMember.member.id.eq(memberId));
		where.and(chatRoomMember.status.eq(MemberRoomStatus.ACTIVE));

		// 2) 메세지 없는 방 숨김 (첫 메시지 이후 활성화된 방만 조회)
		where.and(chatRoom.lastMessageAt.isNotNull());

		// 3) 커서 조건
		//    정렬이 (last_message_at DESC, room_id DESC) 이므로
		//    다음 페이지는 cursor보다 더 과거로 내려가야 한다.
		if (cursorAt != null && cursorRoomId != null) {
			where.and(
				// lt: less than -> 인자보다 작은 값
				chatRoom.lastMessageAt.lt(cursorAt)
					.or(
						// last_message_at이 같으면 room_id로 비교
						chatRoom.lastMessageAt.eq(cursorAt)
							.and(chatRoom.id.lt(cursorRoomId))
					)
			);
		}

		// 실제 쿼리 생성 및 실행
		// Projections는 Querydsl에서 조회 결과를 엔티티가 아니라 내가 만든 DTO로 바로 담아주기 위한 도구
		// DB는 값만 내려주면 Querydsl이 DTO 생성자를 호출해서 객체를 만들어줌
		return queryFactory
			.select(Projections.constructor(
				// MyRoomRaw라는 클래스의 설계도(Class 객체)를 Qureydsl에 넘겨줌
				// Projections.constructor는 생성자를 이용해서 DTO를 만들어줌
				// Projecton을 쓰는 경우 첫 번째 파라미터로 DTO 클래스 타입을 넘겨줘야 함
				MyRoomRow.class,
				chatRoom.id,
				chatRoom.roomType,
				chatRoom.roomName,
				chatRoom.lastMessageId,
				chatRoom.lastMessageSeq,
				chatRoom.lastMessageAt,
				chatRoom.lastMessagePreview,
				chatRoom.lastMessageType,
				chatRoomMember.joinSeq,
				chatRoomMember.lastReadSeq
			))
			.from(chatRoomMember)
			.join(chatRoomMember.room, chatRoom)
			.where(where)
			.orderBy(
				chatRoom.lastMessageAt.desc(),
				chatRoom.id.desc()
			)
			.limit(limitPlusOne)
			.fetch();
	}

	/**
	 * DIRECT 방에서 상대 1명 정보만 한 번에 가져오기 (N+1 문제 해결)
	 * - roomIds에 대해 내가 아닌 멤버를 뽑는다
	 */
	public List<OpponentRow> findOpponentsInDirectRooms(
		Long myMemberId,
		List<Long> roomIds
	) {
		QChatRoomMember chatRoomMember = QChatRoomMember.chatRoomMember;
		QMember member = QMember.member;

		return queryFactory
			.select(Projections.constructor(
				OpponentRow.class,
				chatRoomMember.room.id,
				chatRoomMember.member.id,
				chatRoomMember.member.name,
				chatRoomMember.member.profileImageUrl
			))
			.from(chatRoomMember)
			.where(
				chatRoomMember.room.id.in(roomIds)
					// ne: not equal -> 같지 않은 값
					.and(chatRoomMember.member.id.ne(myMemberId))
					.and(chatRoomMember.status.eq(MemberRoomStatus.ACTIVE))
			)
			.fetch();
	}

}
