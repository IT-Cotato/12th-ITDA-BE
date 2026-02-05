package com.cotato.itda.domain.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.chat.controller.dto.ChatRoomListItemDto;
import com.cotato.itda.domain.chat.controller.dto.ChatRoomSliceResponse;
import com.cotato.itda.domain.chat.controller.dto.DirectRoomResolveResponse;
import com.cotato.itda.domain.chat.controller.dto.OpponentSummaryDto;
import com.cotato.itda.domain.chat.enums.RoomType;
import com.cotato.itda.domain.chat.repository.ChatRoomQueryRepository;
import com.cotato.itda.domain.chat.repository.dto.MyRoomRow;
import com.cotato.itda.domain.chat.repository.dto.OpponentRow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatRoomService {
	private final ChatRoomQueryRepository chatRoomQueryRepository;

	public ChatRoomSliceResponse getMyRooms(
		Long memberId,
		Integer limit,
		LocalDateTime cursorAt,
		Long cursorRoomId
	) {
		// ===== [0] 요청 파라미터 로그 =====
		int safeLimit = (limit == null) ? 10 : Math.min(Math.max(limit, 1), 20);
		int limitPlusOne = safeLimit + 1;

		log.info("[내 채팅방 목록 조회 시작] memberId={}, limit={}, safeLimit={}, cursorAt={}, cursorRoomId={}",
			memberId, limit, safeLimit, cursorAt, cursorRoomId
		);

		// ===== [1] limit+1 조회 =====
		log.info("[내 채팅방 목록 조회] findMyRoomsSlice 호출 -> memberId={}, cursorAt={}, cursorRoomId={}, limitPlusOne={}",
			memberId, cursorAt, cursorRoomId, limitPlusOne
		);

		List<MyRoomRow> fetched = chatRoomQueryRepository.findMyRoomsSlice(
			memberId,
			cursorAt,
			cursorRoomId,
			limitPlusOne
		);

		log.info("[내 채팅방 목록 조회 결과] fetchedSize={}", fetched.size());

		// ===== [2] Slice 판정 =====
		boolean hasNext = fetched.size() > safeLimit;
		List<MyRoomRow> page = hasNext ? fetched.subList(0, safeLimit) : fetched;

		log.info("[Slice 판정] hasNext={}, pageSize={}, safeLimit={}", hasNext, page.size(), safeLimit);

		if (!page.isEmpty()) {
			MyRoomRow first = page.get(0);
			MyRoomRow last = page.get(page.size() - 1);
			log.info("[페이지 범위] 첫번째 roomId={}, lastMessageAt={}, 마지막 roomId={}, lastMessageAt={}",
				first.roomId(), first.lastMessageAt(), last.roomId(), last.lastMessageAt()
			);
		}

		// ===== [3] DIRECT 방 상대 정보 조회 준비 =====
		List<Long> directRoomIds = page.stream()
			.filter(r -> r.roomType() == RoomType.DIRECT)
			.map(MyRoomRow::roomId)
			.toList();

		log.info("[DIRECT 방 추출] memberId={}, directRoomCount={}, directRoomIds={}",
			memberId, directRoomIds.size(), directRoomIds
		);

		Map<Long, OpponentSummaryDto> opponentMap;

		if (directRoomIds.isEmpty()) {
			opponentMap = Map.of();
			log.info("[DIRECT 상대 조회 스킵] directRoomIds 비어있음");
		} else {
			log.info("[DIRECT 상대 조회] findOpponentsInDirectRooms 호출 -> memberId={}, directRoomIds={}",
				memberId, directRoomIds
			);

			List<OpponentRow> opponentRows =
				chatRoomQueryRepository.findOpponentsInDirectRooms(memberId, directRoomIds);

			log.info("[DIRECT 상대 조회 결과] opponentRowsSize={}", opponentRows.size());

			opponentMap = opponentRows.stream()
				.collect(Collectors.toMap(
					OpponentRow::roomId,
					row -> OpponentSummaryDto.builder()
						.memberId(row.memberId())
						.name(row.name())
						.profileImageUrl(row.profileImageUrl())
						.build(),
					(a, b) -> a
				));

			// 어떤 방에 상대가 매핑됐는지 확인용(디버깅 때 매우 도움됨)
			log.info("[DIRECT 상대 매핑 완료] opponentMapKeys(roomIds)={}", opponentMap.keySet());
		}

		// ===== [4] unread_count 계산 + DTO 조립 =====
		List<ChatRoomListItemDto> items = page.stream()
			.map(row -> {
				long effectiveReadSeq = Math.max(row.lastReadSeq(), row.joinSeq());
				long lastMessageSeq = (row.lastMessageSeq() == null) ? 0L : row.lastMessageSeq();
				long unread = Math.max(0L, lastMessageSeq - effectiveReadSeq);

				OpponentSummaryDto opp = (row.roomType() == RoomType.DIRECT)
					? opponentMap.get(row.roomId())
					: null;

				// 각 row마다 핵심값 로그 (너무 많으면 INFO가 과하니, 필요하면 DEBUG로 내려도 됨)
				log.info("[방 아이템 계산] roomId={}, roomType={}, lastMessageSeq={}, lastReadSeq={}, joinSeq={}, effectiveReadSeq={}, unread={}, opponentMemberId={}",
					row.roomId(),
					row.roomType(),
					row.lastMessageSeq(),
					row.lastReadSeq(),
					row.joinSeq(),
					effectiveReadSeq,
					unread,
					(opp == null ? null : opp.memberId())
				);

				return ChatRoomListItemDto.builder()
					.roomId(row.roomId())
					.roomType(row.roomType())
					.roomName(row.roomName())
					.lastMessageId(row.lastMessageId())
					.lastMessageSeq(row.lastMessageSeq())
					.lastMessageAt(row.lastMessageAt())
					.lastMessagePreview(row.lastMessagePreview())
					.lastMessageType(row.lastMessageType())
					.unreadCount(unread)
					.opponent(row.roomType() == RoomType.DIRECT ? opp : null)
					.build();
			})
			.toList();

		log.info("[내 채팅방 목록 DTO 조립 완료] itemsSize={}, hasNext={}", items.size(), hasNext);

		// ===== [5] nextCursor 생성 =====
		LocalDateTime nextCursorAt = null;
		Long nextCursorRoomId = null;

		if (hasNext && !items.isEmpty()) {
			ChatRoomListItemDto last = items.get(items.size() - 1);
			nextCursorAt = last.lastMessageAt();
			nextCursorRoomId = last.roomId();

			log.info("[다음 커서 생성] nextCursorAt={}, nextCursorRoomId={}", nextCursorAt, nextCursorRoomId);
		} else {
			log.info("[다음 커서 없음] hasNext={}, itemsEmpty={}", hasNext, items.isEmpty());
		}

		log.info("[내 채팅방 목록 조회 완료] memberId={}, itemsSize={}, hasNext={}, nextCursorAt={}, nextCursorRoomId={}",
			memberId, items.size(), hasNext, nextCursorAt, nextCursorRoomId
		);

		return ChatRoomSliceResponse.builder()
			.items(items)
			.hasNext(hasNext)
			.nextCursorAt(nextCursorAt)
			.nextCursorRoomId(nextCursorRoomId)
			.build();
	}

	/**
	 *
	 * GET /api/chat/rooms/direct/resolve?opponentMemberId=...
	 * - 방이 있으면 exists=true + roomId
	 * - 없으면 exists=false + null
	 */
	public DirectRoomResolveResponse resolveDirectRoom(Long myMemberId, Long opponentMemberId){

		log.info("[DIRECT 방 resolve 시작] myMemberId={}, opponentMemberId={}", myMemberId, opponentMemberId);

		return chatRoomQueryRepository.resolveDirectRoomId(myMemberId, opponentMemberId)
			.map(roomId -> {
				log.info("[DIRECT 방 resolve 성공] myMemberId={}, opponentMemberId={}, roomId={}",
					myMemberId, opponentMemberId, roomId
				);
				return new DirectRoomResolveResponse(true, roomId);
			})
			.orElseGet(() -> {
				log.info("[DIRECT 방 resolve 실패] myMemberId={}, opponentMemberId={} -> 방 없음",
					myMemberId, opponentMemberId
				);
				return new DirectRoomResolveResponse(false, null);
			});
	}
}
