package com.cotato.itda.domain.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.chat.controller.dto.ChatRoomListItemDto;
import com.cotato.itda.domain.chat.controller.dto.ChatRoomSliceResponse;
import com.cotato.itda.domain.chat.controller.dto.OpponentSummaryDto;
import com.cotato.itda.domain.chat.enums.RoomType;
import com.cotato.itda.domain.chat.repository.ChatRoomQueryRepository;
import com.cotato.itda.domain.chat.repository.dto.MyRoomRow;
import com.cotato.itda.domain.chat.repository.dto.OpponentRow;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {
	private final ChatRoomQueryRepository chatRoomQueryRepository;

	public ChatRoomSliceResponse getMyRooms(
		Long memberId,
		Integer limit,
		LocalDateTime cursorAt,
		Long cursorRoomId
	) {
		int safeLimit = (limit == null) ? 10 : Math.min(Math.max(limit, 1), 20);
		int limitPlusOne = safeLimit + 1;

		// 1) limit+1로 조회해서 Slice 판정 준비
		List<MyRoomRow> fetched = chatRoomQueryRepository.findMyRoomsSlice(
			memberId,
			cursorAt,
			cursorRoomId,
			limitPlusOne
		);

		// 2) Slice 판정
		//   limit+1개가 조회되었으면 다음 페이지가 있다는 뜻
		boolean hasNext = fetched.size() > safeLimit;

		// hasNext 면 마지막 1개는 버림
		List<MyRoomRow> page = hasNext ? fetched.subList(0, safeLimit) : fetched;

		// 3) DIRECT 방만 상대 정보 조회(N+1 문제 방지)
		List<Long> directRoomIds = page.stream()
			.filter(r -> r.roomType() == RoomType.DIRECT)
			.map(MyRoomRow::roomId)
			.toList();

		Map<Long, OpponentSummaryDto> opponentMap = chatRoomQueryRepository
			.findOpponentsInDirectRooms(memberId, directRoomIds)
			.stream()
			.collect(Collectors.toMap(
				OpponentRow::roomId,
				row -> OpponentSummaryDto.builder()
					.memberId(row.memberId())
					.name(row.name())
					.profileImageUrl(row.profileImageUrl())
					.build(),
				// DIRECT는 방당 1명만 기대하지만 ,혹시 중복이 나오면 첫 번째 유지
				(a, b) -> a
			));

		// 4) unread_count 계산 + 응답 DTO 조립
		List<ChatRoomListItemDto> items = page.stream()
			.map(row -> {
				// effectiveReadSeq: 내가 읽은 마지막 메세지 시퀀스 = max(last_read_seq, join_seq)
				long effectiveReadSeq = Math.max(row.lastReadSeq(), row.joinSeq());

				// lastMessageSeq가 null이면 0으로 간주
				long lastMessageSeq = (row.lastMessageSeq() == null) ? 0L : row.lastMessageSeq();

				long unread = Math.max(0L, lastMessageSeq - effectiveReadSeq);

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
					.opponent(row.roomType() == RoomType.DIRECT ? opponentMap.get(row.roomId()) : null)
					.build();
			})
			.toList();

		// 5) nextCursor 생성
		LocalDateTime nextCursorAt = null;
		Long nextCursorRoomId = null;

		if (hasNext && !items.isEmpty()) {
			// 이번 페이지의 마지막 아이템이 다음 페이지의 커서
			ChatRoomListItemDto last = items.get(items.size() - 1);
			nextCursorAt = last.lastMessageAt();
			nextCursorRoomId = last.roomId();
		}

		return ChatRoomSliceResponse.builder()
			.items(items)
			.hasNext(hasNext)
			.nextCursorAt(nextCursorAt)
			.nextCursorRoomId(nextCursorRoomId)
			.build();
	}
}
