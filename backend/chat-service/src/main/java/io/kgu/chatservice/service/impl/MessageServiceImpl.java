package io.kgu.chatservice.service.impl;

import com.amazonaws.util.Base64;
import io.kgu.chatservice.domain.dto.ChatDto;
import io.kgu.chatservice.domain.dto.MessageDto;
import io.kgu.chatservice.domain.entity.ChatEntity;
import io.kgu.chatservice.domain.entity.MessageContentType;
import io.kgu.chatservice.domain.entity.MessageEntity;
import io.kgu.chatservice.repository.ChatRepository;
import io.kgu.chatservice.repository.MessageRepository;
import io.kgu.chatservice.service.AmazonS3Service;
import io.kgu.chatservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.AbstractWebSocketMessage;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final AmazonS3Service amazonS3Service;
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ModelMapper mapper;

    @Override
    public MessageDto create(AbstractWebSocketMessage<?> message, ChatEntity chat, String from) {

        MessageEntity messageEntity = new MessageEntity();

        if (message instanceof TextMessage) {
            messageEntity.setType(MessageContentType.TEXT);
            messageEntity.setContent(((TextMessage)message).getPayload());

        } else if (message instanceof BinaryMessage) {
            messageEntity.setType(MessageContentType.IMAGE);

            ByteBuffer payload = (ByteBuffer) message.getPayload();

            byte[] decodedBase64String = Base64.decode(payload.array());

            try {
                String url = amazonS3Service.upload(decodedBase64String, from +"image"+ UUID.randomUUID());
                messageEntity.setContent(url);
            } catch (IOException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
            }
        } else {
            throw new ClassFormatError("잘못된 메세지 포맷");
        }

        messageEntity.setFromUser(from);
        messageEntity.setCreatedAt(LocalDateTime.now());
        messageEntity.setChat(chat);

        MessageEntity result = messageRepository.save(messageEntity);

        return mapper.map(result, MessageDto.class);
    }

    @Override
    public List<MessageDto> findAllMessagesByChatRoom(ChatDto chatDto) {
        ChatEntity chatRoom = chatRepository.findChatEntityBySessionId(chatDto.getSessionId());

        List<MessageEntity> result = messageRepository.findAllByChat(chatRoom);

        return result.stream()
                .map(m -> mapper.map(m, MessageDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageDto> syncAllMessagesByChatRoomAndDate(ChatDto chatDto, LocalDateTime date) {
        ChatEntity chatRoom = chatRepository.findChatEntityBySessionId(chatDto.getSessionId());


        List<MessageEntity> result = messageRepository.findAllByChatAndCreatedAtAfter(chatRoom, date);

        return result.stream()
                .map(m -> mapper.map(m, MessageDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public int trimMessagesByDate(LocalDateTime date) {
        return messageRepository.deleteAllByCreatedAtBefore(date);
    }

}
