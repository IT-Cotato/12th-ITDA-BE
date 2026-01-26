package com.cotato.itda.global.model.draft;

import java.time.OffsetDateTime;

/**
 * createdAt/updatedAt 등의 메타 정보
 */
public record DraftMeta(
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
	public DraftMeta withUpdatedAt() {
		return new DraftMeta(this.createdAt, OffsetDateTime.now());
	}
}
