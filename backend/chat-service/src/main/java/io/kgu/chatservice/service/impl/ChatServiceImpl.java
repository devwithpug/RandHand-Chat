package io.kgu.chatservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final ModelMapper modelMapper;
    private final KafkaProducer kafkaProducer;

    @Override
    public QueueDto makeChatQueue(QueueDto queueDto) throws JsonProcessingException {

        if (queueDto.getGesture() == null || queueDto.getUserId() == null) {
            throw new IllegalArgumentException(String.format(
                    "잘못된 요청입니다 'userId: %s, gesture: %s'", queueDto.getUserId(), queueDto.getGesture()
            ));
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
    public ChatDto getOneChatRoomBySessionId(String sessionId) {

        ChatEntity chatEntity = chatRepository.findBySessionId(sessionId);

        if (chatEntity == null) {
            throw new EntityNotFoundException(String.format(
                    "일치하는 채팅방이 없습니다 'sessionId: %s'", sessionId
            ));
        }

        return modelMapper.map(chatEntity, ChatDto.class);
    }

    @Override
    public ChatDto getOneChatRoomByUserId(String userId) {

        ChatEntity chatEntity = chatRepository.findByUserId(userId);

        if (chatEntity == null) {
            throw new EntityNotFoundException(String.format(
                    "일치하는 채팅방이 없습니다 'userId: %s'", userId
            ));
        }

        return modelMapper.map(chatEntity, ChatDto.class);
    }

    @Override
    public void removeChatRoomBySessionId(String sessionId) {

        chatRepository.deleteBySessionId(sessionId);

    }
}
