package com.cotato.itda.domain.chattopic.converter;

import com.cotato.itda.domain.chattopic.dto.res.ChatTopicResDTO;
import com.cotato.itda.domain.chattopic.entity.ChatTopic;

import java.util.List;

public class ChatTopicConverter {

    // Entity -> DTO
    public static ChatTopicResDTO.ChatTopicDTO toChatTopicDTO(ChatTopic chatTopic) {
        return ChatTopicResDTO.ChatTopicDTO.builder()
                .name(chatTopic.getName())
                .code(chatTopic.getCode())
                .build();
    }

    public static ChatTopicResDTO.ChatTopicListDTO toChatTopicListDTO(List<ChatTopic> chatTopics) {
        return ChatTopicResDTO.ChatTopicListDTO.builder()
                .count(chatTopics.size())
                .chatTopicList(chatTopics.stream()
                        .map(ChatTopicConverter::toChatTopicDTO)
                        .toList())
                .build();
    }
}
