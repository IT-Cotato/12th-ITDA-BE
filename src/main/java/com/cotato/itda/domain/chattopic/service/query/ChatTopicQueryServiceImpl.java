package com.cotato.itda.domain.chattopic.service.query;

import com.cotato.itda.domain.chattopic.converter.ChatTopicConverter;
import com.cotato.itda.domain.chattopic.dto.res.ChatTopicResDTO;
import com.cotato.itda.domain.chattopic.entity.ChatTopic;
import com.cotato.itda.domain.chattopic.repository.ChatTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatTopicQueryServiceImpl implements ChatTopicQueryService {

    private final ChatTopicRepository chatTopicRepository;

    @Override
    public ChatTopicResDTO.ChatTopicListDTO getChatTopics() {
        List<ChatTopic> chatTopics = chatTopicRepository.findAllByIsActiveTrue();

        return ChatTopicConverter.toChatTopicListDTO(chatTopics);
    }
}
