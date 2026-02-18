package com.cotato.itda.domain.chat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cotato.itda.domain.chat.entity.ChatMessageAttachment;

public interface ChatMessageAttachmentRepository extends JpaRepository<ChatMessageAttachment,Long> {
	interface AttachmentAccessView {
		Long getAttachmentId();
		Long getRoomId();
		Long getMessageId();
		Long getMessageSeq();
		String getObjectKey();
		String getMimeType();
		Long getSizeBytes();
	}

	@Query(value = """
    select
      a.id as attachmentId,
      m.chat_room_id as roomId,
      m.id as messageId,
      m.message_seq as messageSeq,
      a.object_key as objectKey,
      a.mime_type as mimeType,
      a.size_bytes as sizeBytes
    from chat_message_attachment a
    join chat_message m on m.id = a.chat_message_id
    where a.object_key = :objectKey
""", nativeQuery = true)
	Optional<AttachmentAccessView> findAccessViewByObjectKey(@Param("objectKey") String objectKey);

}
