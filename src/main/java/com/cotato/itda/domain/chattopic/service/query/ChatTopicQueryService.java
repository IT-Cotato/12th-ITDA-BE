package com.cotato.itda.domain.chattopic.service.query;

import com.cotato.itda.domain.chattopic.dto.res.ChatTopicResDTO;

public interface ChatTopicQueryService {
    ChatTopicResDTO.ChatTopicListDTO getChatTopics();
}
