package io.kgu.chatservice.service.impl;

import io.kgu.chatservice.domain.dto.ChatDto;
import io.kgu.chatservice.domain.dto.QueueDto;
import io.kgu.chatservice.domain.entity.ChatEntity;
import io.kgu.chatservice.messagequeue.KafkaProducer;
import io.kgu.chatservice.repository.ChatRepository;
import io.kgu.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.InvalidPropertiesFormatException;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final ModelMapper modelMapper;
    private final KafkaProducer kafkaProducer;

    @Override
    public QueueDto makeChatQueue(QueueDto queueDto) {

        if (queueDto.getGesture() == null || queueDto.getUserId() == null) {
            throw new IllegalArgumentException("Invalid request from queueDto: " + queueDto);
        }

        kafkaProducer.sendQueue(queueDto);

        return queueDto;
    }

    @Override
    public ChatDto createChatRoom(ChatDto chatDto) {

        if (chatDto.getSessionId() != null) {
            throw new IllegalArgumentException("생성하려는 ChatDto.sessionId 가 이미 존재합니다.");
        }

        chatDto.setSessionId(LocalDate.now() + ":" + UUID.randomUUID());

        ChatEntity entity = modelMapper.map(chatDto, ChatEntity.class);
        chatRepository.save(entity);

        log.info("ChatRoom created : " + entity);

        return chatDto;
    }

    @Override
    public ChatDto getOneChatRoom(String sessionId) {

        ChatEntity chatEntity = chatRepository.findBySessionId(sessionId);

        if (chatEntity == null) {
            throw new NoSuchElementException("해당 sessionId 와 일치하는 ChatRoom 이 존재하지 않습니다: " + sessionId);
        }
        return modelMapper.map(chatEntity, ChatDto.class);
    }
}
