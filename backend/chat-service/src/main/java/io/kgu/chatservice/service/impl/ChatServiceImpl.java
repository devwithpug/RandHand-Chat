package io.kgu.chatservice.service.impl;

import io.kgu.chatservice.domain.dto.chat.ChatDto;
import io.kgu.chatservice.domain.entity.ChatEntity;
import io.kgu.chatservice.repository.ChatRepository;
import io.kgu.chatservice.repository.MessageRepository;
import io.kgu.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ModelMapper mapper;

    @Override
    public ChatDto createChatRoom(ChatDto chatDto) {

        if (chatDto.getSessionId() != null) {
            throw new IllegalArgumentException("생성하려는 ChatDto.sessionId 가 이미 존재합니다.");
        }

        chatDto.setSessionId(LocalDate.now() + ":" + UUID.randomUUID());
        chatDto.setSyncTime(LocalDateTime.now());

        ChatEntity entity = mapper.map(chatDto, ChatEntity.class);
        chatRepository.save(entity);

        log.info("ChatRoom created : " + entity);

        return chatDto;
    }

    @Override
    public ChatDto getOneChatRoomBySessionId(String sessionId) {

        ChatEntity chatEntity = chatRepository.findChatEntityBySessionId(sessionId);

        if (chatEntity == null) {
            throw new EntityNotFoundException(String.format(
                    "일치하는 채팅방이 없습니다 'sessionId: %s'", sessionId
            ));
        }

        return mapper.map(chatEntity, ChatDto.class);
    }

    @Override
    public List<ChatDto> getAllChatRoomByUserId(String userId) {

        List<ChatEntity> result = chatRepository.findAllByUserId(userId);

        if (result.isEmpty()) {
            throw new EntityNotFoundException(String.format(
                    "일치하는 채팅방이 없습니다 'userId: %s'", userId
            ));
        }

        return result.stream()
                .map(c -> mapper.map(c, ChatDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void removeChatRoomBySessionId(String sessionId) {

        ChatEntity chatRoom = chatRepository.findById(sessionId).orElseThrow(
                () -> new EntityNotFoundException(String.format(
                        "삭제하려는 채팅방이 없습니다 'sessionId: %s'", sessionId
                )));

        messageRepository.deleteAllByChat(chatRoom);
        chatRepository.deleteBySessionId(sessionId);
    }
}
